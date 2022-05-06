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

package com.ibm.cloud.appconfiguration.android.sdk.configurations

import android.content.Context
import com.ibm.cloud.appconfiguration.android.sdk.AppConfiguration
import com.ibm.cloud.appconfiguration.android.sdk.configurations.internal.*
import com.ibm.cloud.appconfiguration.android.sdk.configurations.models.Feature
import com.ibm.cloud.appconfiguration.android.sdk.configurations.models.Property
import com.ibm.cloud.appconfiguration.android.sdk.configurations.models.internal.Segment
import com.ibm.cloud.appconfiguration.android.sdk.configurations.models.internal.SegmentRules
import com.ibm.cloud.appconfiguration.android.sdk.core.Logger
import com.ibm.cloud.appconfiguration.android.sdk.core.ServiceImpl
import com.ibm.cloud.sdk.core.http.Response
import org.apache.commons.codec.digest.MurmurHash3
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


/**
 * Internal class to handle the configuration.
 */
internal class ConfigurationHandler {

    private lateinit var collectionId: String
    private lateinit var environmentId: String
    private lateinit var appContext: Context
    private var configurationUpdateListener: ConfigurationUpdateListener? = null
    private var isInitialized = false
    private lateinit var urlBuilder: URLBuilder
    private var featureMap: HashMap<String, Feature> = HashMap()
    private var propertyMap: HashMap<String, Property> = HashMap()
    private var segmentMap: HashMap<String, Segment> = HashMap()
    private var metering: Metering? = null
    private var retryCount = 3
    private var fileManager: FileManagerInterface = FileManager

    companion object Factory {
        private var instance: ConfigurationHandler? = null

        /**
         * @return instance of [ConfigurationHandler]
         */
        fun getInstance(): ConfigurationHandler {
            if (instance == null)
                instance =
                    ConfigurationHandler()
            return instance!!
        }
    }

    /**
     * Initialize the configurations.
     *
     * @param context application context
     * @param collectionId collection id
     * @param environmentId environment id
     */
    fun init(context: Context, collectionId: String, environmentId: String) {
        appContext = context
        this.collectionId = collectionId
        this.environmentId = environmentId
        urlBuilder = URLBuilder.getInstance()
        urlBuilder.init(collectionId, environmentId)
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

    /**
     * Returns all features.
     *
     * @return hashmap of all features and their corresponding [Feature] objects
     */
    fun getFeatures(): HashMap<String, Feature> {
        if (featureMap.size == 0) {
            loadConfigurations()
        }
        return featureMap
    }

    /**
     * Returns the [Feature] object with the details of the feature specified by the `featureId`.
     *
     * @param featureId the Feature Id
     * @return
     */
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

    /**
     * Returns all properties.
     *
     * @return hashmap of all properties and their corresponding [Property] objects
     */
    fun getProperties(): HashMap<String, Property> {
        return propertyMap
    }

    /**
     * Returns the [Property] object with the details of the property specified by the `propertyId`.
     *
     * @param propertyId the Property Id
     * @return
     */
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
        val allConfigs = fileManager.getFileData(appContext) ?: return

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

    /**
     * Records each of feature and property evaluations done by sending it to [Metering].
     *
     * @param featureId feature id
     * @param propertyId property id
     * @param entityId entity id
     * @param segmentId segment id
     */
    fun recordEvaluation(
        featureId: String?,
        propertyId: String?,
        entityId: String,
        segmentId: String
    ) {
        metering?.addMetering(
            AppConfiguration.getInstance().getGuid(),
            this.environmentId,
            this.collectionId,
            entityId,
            segmentId,
            featureId,
            propertyId
        )
    }

    /**
     * Property evaluation.
     *
     * @param property object of [Property] class
     * @param entityId entity id
     * @param entityAttributes entity attributes JSON object
     * @return property evaluated value
     */
    fun propertyEvaluation(
        property: Property,
        entityId: String,
        entityAttributes: JSONObject?
    ): Any? {

        var resultDict = JSONObject()
        resultDict.put(ConfigConstants.EVALUATED_SEGMENT_ID, ConfigConstants.DEFAULT_SEGMENT_ID)
        resultDict.put(ConfigConstants.VALUE, Any())

        try {
            val segmentRules = property.getSegmentRules()
            if (segmentRules.length() > 0 && entityAttributes != null &&
                entityAttributes.length() != 0) {
                val rulesMap: HashMap<Int, SegmentRules> = parseRules(segmentRules)
                resultDict = evaluateRules(rulesMap, entityAttributes, null, property, null)
                return resultDict[ConfigConstants.VALUE]
            }
            return property.getPropertyValue()
        } finally {
            val propertyId = property.getPropertyId()
            this.recordEvaluation(
                null,
                propertyId,
                entityId,
                resultDict.getString(ConfigConstants.EVALUATED_SEGMENT_ID)
            )
        }
    }

    /**
     * Feature evaluation.
     *
     * @param feature object of [Feature] class
     * @param isEnabled enabled value of [Feature] class object (true/false)
     * @param entityId entity id
     * @param entityAttributes entity attributes JSON object
     * @return feature evaluated value
     */
    fun featureEvaluation(
        feature: Feature,
        entityId: String,
        entityAttributes: JSONObject?
    ): Any? {

        var resultDict = JSONObject()
        resultDict.put(ConfigConstants.EVALUATED_SEGMENT_ID, ConfigConstants.DEFAULT_SEGMENT_ID)
        resultDict.put(ConfigConstants.VALUE, Any())
        val map = HashMap<String, Any>()
        try {
            if (feature.isEnabled()) {
                val segmentRules = feature.getSegmentRules()
                if (segmentRules.length() > 0 && entityAttributes != null &&
                    entityAttributes.length() != 0) {
                    //if target rules configured then we evaluate as per segments
                    val rulesMap: HashMap<Int, SegmentRules> = parseRules(segmentRules)
                    resultDict = this.evaluateRules(rulesMap, entityAttributes, feature, null, entityId)
                    map[ConfigConstants.CURRENT_VALUE] = resultDict.get(ConfigConstants.VALUE)
                    map[ConfigConstants.IS_ENABLED] = resultDict.get(ConfigConstants.FEATURE_ENABLED)
                    return map
                }
                if (feature.getRolloutPercentage() == ConfigConstants.DEFAULT_ROLLOUT_PERCENTAGE ||
                        generateHash(entityId, feature.getFeatureId()) <
                        feature.getRolloutPercentage()) {
                        map[ConfigConstants.CURRENT_VALUE] = feature.getFeatureEnabledValue()
                        map[ConfigConstants.IS_ENABLED] = true
                        return map
                }
                map[ConfigConstants.CURRENT_VALUE] = feature.getFeatureDisabledValue()
                map[ConfigConstants.IS_ENABLED] = false
                return map
            } else {
                map[ConfigConstants.CURRENT_VALUE] = feature.getFeatureDisabledValue()
                map[ConfigConstants.IS_ENABLED] = false
                return map
            }
        } finally {
            val featureId = feature.getFeatureId()
            this.recordEvaluation(
                featureId,
                null,
                entityId,
                resultDict.getString(ConfigConstants.EVALUATED_SEGMENT_ID)
            )
        }
    }

    private fun generateHash(entityId: String, featureId: String): Int {
        val uniqueId = "$entityId:$featureId"
        val hashValue = MurmurHash3.hash32x86(
            uniqueId.encodeToByteArray(), ConfigConstants.OFFSET,
            uniqueId.encodeToByteArray().size, ConfigConstants.SEED
        )
        // The hash value generated from MurmurHash3.hash32x86() is signed Int whose range is -2^16 to +2^16
        // But we need hash value from 0 to +2^32 -1
        // This conversion from signed Int to unsigned Int using toUInt() is supported only from Kotlin 1.5 & above
        // For us to have consistent output in all Kotlin versions, we restrain from using toUInt().
        // And have written our own logic that is compatible in all the Kotlin versions
        if (hashValue < 0) {
            val hashValueL: Long = hashValue.toLong() + 0x00000000ffffffffL + 1
            return (hashValueL / ConfigConstants.MAX_VAL * 100).toInt()
        }
        return (hashValue / ConfigConstants.MAX_VAL * 100).toInt()
    }

    private fun evaluateRules(
        rulesMap: HashMap<Int, SegmentRules>,
        entityAttributes: JSONObject?,
        feature: Feature?,
        property: Property?,
        entityId: String?
    ): JSONObject {

        val resultDict = JSONObject()
        resultDict.put(ConfigConstants.EVALUATED_SEGMENT_ID, ConfigConstants.DEFAULT_SEGMENT_ID)
        resultDict.put(ConfigConstants.VALUE, Any())

        try {

            for (i in 1..rulesMap.size) {
                val segmentRule = rulesMap[i]
                if (segmentRule != null) {
                    for (level in 0 until segmentRule.getRules().length()) {

                        val rule = segmentRule.getRules().getJSONObject(level)
                        val segments = rule.getJSONArray("segments")

                        for (innerLevel in 0 until segments.length()) {

                            val segmentId = segments.getString(innerLevel)

                            if (evaluateSegment(segmentId, entityAttributes)) {
                                resultDict.put(ConfigConstants.EVALUATED_SEGMENT_ID, segmentId)
                                    if (feature != null) {
                                        val segmentRolloutPercentage =
                                            getInheritedSegmentRolloutPercentage(segmentRule
                                                .getRolloutPercentage(), feature.getRolloutPercentage())
                                        if (segmentRolloutPercentage == ConfigConstants
                                                .DEFAULT_ROLLOUT_PERCENTAGE ||
                                                generateHash(entityId!!, feature.getFeatureId()) <
                                                segmentRolloutPercentage) {
                                            if (segmentRule.getValue() == "\$default") {
                                                resultDict.put(ConfigConstants.VALUE,
                                                    feature.getFeatureEnabledValue())
                                            } else {
                                                resultDict.put(ConfigConstants.VALUE,
                                                    segmentRule.getValue())
                                            }
                                            resultDict.put(ConfigConstants.FEATURE_ENABLED, true)
                                        } else {
                                            resultDict.put(ConfigConstants.VALUE, feature
                                                .getFeatureDisabledValue())
                                            resultDict.put(ConfigConstants.FEATURE_ENABLED, false)
                                        }
                                    } else {
                                        if (segmentRule.getValue() == "\$default") {
                                            resultDict.put(ConfigConstants.VALUE, property!!
                                                .getPropertyValue())
                                        } else {
                                            resultDict.put(ConfigConstants.VALUE, segmentRule.getValue())
                                        }
                                    }
                                return resultDict
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Logger.error("RuleEvaluation $e")
        }

        if (feature != null) {
            if (feature.getRolloutPercentage() == ConfigConstants.DEFAULT_ROLLOUT_PERCENTAGE ||
                    generateHash(entityId!!, feature.getFeatureId()) < feature.getRolloutPercentage()) {
                resultDict.put(ConfigConstants.VALUE, feature.getFeatureEnabledValue())
                resultDict.put(ConfigConstants.FEATURE_ENABLED, true)
            } else {
                resultDict.put(ConfigConstants.VALUE, feature.getFeatureDisabledValue())
                resultDict.put(ConfigConstants.FEATURE_ENABLED, false)
            }
        } else {
            resultDict.put(ConfigConstants.VALUE, property!!.getPropertyValue())
        }

        return resultDict
    }

    private fun getInheritedSegmentRolloutPercentage(segmentRolloutPercentage: Any,
                                              featureRolloutPercentage: Int):
    Int {
        if (segmentRolloutPercentage == "\$default") {
           return featureRolloutPercentage
        }
        return segmentRolloutPercentage as Int
    }

    private fun evaluateSegment(segmentId: String, entityAttributes: JSONObject?): Boolean {
        if (segmentMap.containsKey(segmentId)) {
            val segment = segmentMap[segmentId]
            return segment!!.evaluateRule(entityAttributes)
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

    private fun writeToFile(jsonData: JSONObject) {
        fileManager.storeFiles(appContext, jsonData.toString())
        loadConfigurations()
        if (configurationUpdateListener != null) {
            configurationUpdateListener?.onConfigurationUpdate()
        }
    }

    private fun fetchFromAPI() {
        if (isInitialized) {
            val configURL = urlBuilder.getConfigUrl()
            val response: Response<String>? = ServiceImpl.getInstance().getConfig(configURL)
            if (response != null) {
                if (response.statusCode >= ConfigConstants.REQUEST_SUCCESS_200
                    && response.statusCode <= ConfigConstants.REQUEST_SUCCESS_299
                ) {
                    Logger.debug("Successfully fetched the configurations with response statusCode=${response.statusCode}")
                    var responseText: JSONObject? = null
                    try {
                        responseText = JSONObject(response.result)
                    } catch (e: JSONException) {
                        Logger.error("Error decoding the server response. ${e.message}")
                    }
                    responseText?.let { writeToFile(it) }
                } else {
                    Logger.error(ConfigMessages.CONFIG_API_ERROR)
                }
            } else {
                Logger.error(ConfigMessages.CONFIG_API_ERROR)
            }
        } else {
            Logger.error(ConfigMessages.CONFIG_HANDLER_INIT_ERROR)
        }
    }
}