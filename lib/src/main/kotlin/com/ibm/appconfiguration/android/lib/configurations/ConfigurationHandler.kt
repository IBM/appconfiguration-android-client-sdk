/**
 * Copyright 2021 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.appconfiguration.android.lib.configurations

import android.content.Context
import com.ibm.appconfiguration.android.lib.AppConfiguration
import com.ibm.appconfiguration.android.lib.configurations.internal.*
import com.ibm.appconfiguration.android.lib.configurations.internal.FileManager
import com.ibm.appconfiguration.android.lib.configurations.internal.Metering
import com.ibm.appconfiguration.android.lib.configurations.internal.URLBuilder
import com.ibm.appconfiguration.android.lib.configurations.models.Feature
import com.ibm.appconfiguration.android.lib.configurations.models.Property
import com.ibm.appconfiguration.android.lib.configurations.models.internal.Segment
import com.ibm.appconfiguration.android.lib.configurations.models.internal.SegmentRules
import com.ibm.appconfiguration.android.lib.core.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


internal class ConfigurationHandler {

    private lateinit var collectionId: String
    private lateinit var appContext: Context
    private var configurationUpdateListener: ConfigurationUpdateListener? = null
    private var isInitialized = false
    private lateinit var urlBuilder: URLBuilder
    private var featureMap: HashMap<String, Feature> = HashMap()
    private var propertyMap: HashMap<String, Property> = HashMap()
    private var segmentMap: HashMap<String, Segment> = HashMap()
    private var metering: Metering? = null
    private var retryCount = 3

    companion object Factory {
        private var instance: ConfigurationHandler? = null
        fun getInstance(): ConfigurationHandler {
            if (instance == null)
                instance =
                    ConfigurationHandler()
            return instance!!
        }
    }

    fun init(context: Context, collectionId: String) {
        appContext = context
        this.collectionId = collectionId
        urlBuilder = URLBuilder.getInstance()
        urlBuilder.init(collectionId)
        featureMap = HashMap()
        propertyMap = HashMap()
        segmentMap = HashMap()
        this.metering = Metering.getInstance()
        this.metering?.init(context)
        this.isInitialized = true
    }

    fun registerConfigurationUpdateListener(listener: ConfigurationUpdateListener) {
        if (isInitialized) {
            configurationUpdateListener = listener
        } else {
            Logger.error(ConfigMessages.CONFIG_HANDLER_INIT_ERROR)
        }
    }

    fun getFeatures(): HashMap<String, Feature>? {
        return featureMap
    }

    fun getFeature(featureId: String?): Feature? {
        return if (featureMap.containsKey(featureId)) {
            featureMap[featureId]
        } else {
            loadConfigurations()
            if (featureMap.containsKey(featureId)) {
                featureMap[featureId]
            } else {
                Logger.error(ConfigMessages.FEATURE_INVALID + featureId)
                null
            }
        }
    }

    fun getProperties(): HashMap<String, Property>? {
        return propertyMap
    }

    fun getProperty(propertyId: String?): Property? {
        return if (propertyMap.containsKey(propertyId)) {
            propertyMap[propertyId]
        } else {
            loadConfigurations()
            if (propertyMap.containsKey(propertyId)) {
                propertyMap[propertyId]
            } else {
                Logger.error(ConfigMessages.PROPERTY_INVALID + propertyId)
                null
            }
        }
    }

    fun fetchConfigurations() {
        if (this.isInitialized) {
            loadConfigurations()
            fetchFromAPI()
            retryCount = 3
        } else {
            Logger.error(ConfigMessages.CONFIG_HANDLER_INIT_ERROR)
        }
    }

    private fun loadConfigurations() {
        val allConfigs = FileManager.getFileData(appContext) ?: return

        if (allConfigs.has("features")) {
            try {
                val allFeatureList = allConfigs.optJSONArray("features") ?: return
                for (i in 0 until allFeatureList.length()) {
                    val feature: JSONObject = allFeatureList.getJSONObject(i)
                    val featureObj = Feature(feature)
                    featureMap[featureObj.getFeatureId()] = featureObj
                }
            } catch (e: JSONException) {
                Logger.error("${e.message}")
            }
        }

        if (allConfigs.has("properties")) {
            try {
                val allPropertyList = allConfigs.optJSONArray("properties") ?: return
                for (i in 0 until allPropertyList.length()) {
                    val property: JSONObject = allPropertyList.getJSONObject(i)
                    val propertyObj = Property(property)
                    propertyMap[propertyObj.getPropertyId()] = propertyObj
                }
            } catch (e: JSONException) {
                Logger.error("${e.message}")
            }
        }

        if (allConfigs.has("segments")) {
            try {
                val segmentList = allConfigs.optJSONArray("segments") ?: return
                for (i in 0 until segmentList.length()) {
                    val segment = segmentList.getJSONObject(i)
                    val segmentObj =
                        Segment(
                            segment
                        )
                    segmentMap[segmentObj.segment_id] = segmentObj
                }
            } catch (e: JSONException) {
                Logger.error("${e.message}")
            }
        }
    }

    private fun recordEvaluation(
        featureId: String?,
        propertyId: String?,
        identityId: String,
        segmentId: String
    ) {
        metering?.addMetering(
            AppConfiguration.getInstance().getGuid(),
            collectionId,
            identityId,
            segmentId,
            featureId,
            propertyId
        )
    }

    fun propertyEvaluation(property: Property, identityId: String, identityAttributes: JSONObject): Any? {

        var resultDict = JSONObject()
        resultDict.put("evaluated_segment_id", ConfigConstants.DEFAULT_SEGMENT_ID)
        resultDict.put("value", Any())

        try {

            if (identityAttributes == null || identityAttributes.length() == 0) {
                return property.getPropertyValue();
            }

            val segmentRules = property.getSegmentRules()
            return if (segmentRules.length() > 0) {
                val rulesMap: HashMap<Int, SegmentRules> = parseRules(segmentRules)
                resultDict = evaluateRules(rulesMap, identityAttributes, null, property)
                resultDict["value"]
            } else {
                property.getPropertyValue()
            }
        } finally {
            val propertyId = property.getPropertyId()
            this.recordEvaluation(
                null,
                propertyId,
                identityId,
                resultDict.getString("evaluated_segment_id")
            )
        }
    }

    fun featureEvaluation(feature: Feature, identityId: String, identityAttributes: JSONObject): Any? {

        var resultDict = JSONObject()
        resultDict.put("evaluated_segment_id", ConfigConstants.DEFAULT_SEGMENT_ID)
        resultDict.put("value", Any())

        try {
            if (feature.isEnabled()) {

                if (identityAttributes == null || identityAttributes.length() == 0) {
                    return feature.getFeatureEnabledValue();
                }
                val segmentRules = feature.getSegmentRules()
                return if (segmentRules.length() > 0) {
                    val rulesMap: HashMap<Int, SegmentRules> = parseRules(segmentRules)
                    resultDict = this.evaluateRules(rulesMap, identityAttributes, feature, null)
                    resultDict["value"]
                } else {
                    feature.getFeatureEnabledValue()
                }

            } else {
                return feature.getFeatureDisabledValue();
            }
        } finally {
            val featureId = feature.getFeatureId()
            this.recordEvaluation(
                featureId,
                null,
                identityId,
                resultDict.getString("evaluated_segment_id")
            )
        }
    }

    private fun evaluateRules(
        rulesMap: HashMap<Int, SegmentRules>,
        identityAttributes: JSONObject,
        feature: Feature?,
        property: Property?
    ): JSONObject {

        val resultDict = JSONObject()
        resultDict.put("evaluated_segment_id", ConfigConstants.DEFAULT_SEGMENT_ID)
        resultDict.put("value", Any())

        try {

            for (i in 1..rulesMap.size) {

                val segmentRule = rulesMap[i]
                if (segmentRule != null) {
                    for (level in 0 until segmentRule.getRules().length()) {

                        val rule = segmentRule.getRules().getJSONObject(level)
                        val segments = rule.getJSONArray("segments")

                        for (innerLevel in 0 until segments.length()) {

                            val segmentKey = segments.getString(innerLevel)

                            if (evaluateSegment(segmentKey, identityAttributes)) {
                                resultDict.put("evaluated_segment_id", segmentKey);
                                if (segmentRule.getValue() == "\$default") {
                                    if (feature != null) {
                                        resultDict.put("value", feature.getFeatureEnabledValue());
                                    } else {
                                        resultDict.put("value", property!!.getPropertyValue());
                                    }
                                } else {
                                    resultDict.put("value", segmentRule.getValue());
                                }
                                return resultDict;
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Logger.error("RuleEvaluation $e")
        }

        if (feature != null) {
            resultDict.put("value", feature.getFeatureEnabledValue());
        } else {
            resultDict.put("value", property!!.getPropertyValue());
        }

        return resultDict;
    }

    private fun evaluateSegment(segmentKey: String, identityAttributes: JSONObject): Boolean {
        if (segmentMap.containsKey(segmentKey)) {
            val segment = segmentMap[segmentKey]
            return segment!!.evaluateRule(identityAttributes)
        }
        return false
    }

    private fun parseRules(segmentRules: JSONArray): HashMap<Int, SegmentRules> {
        val rulesMap: HashMap<Int, SegmentRules> = HashMap<Int, SegmentRules>()
        for (i in 0 until segmentRules.length()) {
            try {
                val rules = segmentRules.getJSONObject(i)
                val rulesObj =
                    SegmentRules(
                        rules
                    )
                rulesMap[rulesObj.getOrder()] = rulesObj
            } catch (e: JSONException) {
                Logger.error("${e.message}")
            }
        }
        return rulesMap
    }

    fun writeToFile(jsonData: JSONObject) {
        FileManager.storeFiles(appContext, jsonData.toString())
        loadConfigurations()
        if (configurationUpdateListener != null) {
            configurationUpdateListener?.onConfigurationUpdate()
        }
    }

    private fun fetchFromAPI() {
        if (isInitialized) {

            val configURL = urlBuilder.getConfigUrl()
            val apiManager: APIManager =
                APIManager.newInstance(appContext, configURL, BaseRequest.GET)
            apiManager.setResponseListener(object : ResponseListener {
                override fun onSuccess(response: Response) {
                    var responseText: JSONObject? = null
                    try {
                        responseText = JSONObject(response.getResponseText() ?: "")
                    } catch (e: JSONException) {
                        Logger.error("Error decoding the server response. ${e.message}")
                    }
                    responseText?.let { writeToFile(it) }
                }

                override fun onFailure(
                    response: Response?,
                    t: Throwable?,
                    extendedInfo: JSONObject?
                ) {
                    Logger.error(ConfigMessages.CONFIG_API_ERROR)
                }

            })
            apiManager.execute()
        } else {
            Logger.error(ConfigMessages.CONFIG_HANDLER_INIT_ERROR)
        }
    }
}