/*
 * (C) Copyright IBM Corp. 2021.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.ibm.appconfiguration.android.lib.feature

import android.content.Context
import com.ibm.appconfiguration.android.lib.AppConfiguration
import com.ibm.appconfiguration.android.lib.core.*
import com.ibm.appconfiguration.android.lib.feature.internal.FileManager
import com.ibm.appconfiguration.android.lib.feature.internal.Metering
import com.ibm.appconfiguration.android.lib.feature.internal.URLBuilder
import com.ibm.appconfiguration.android.lib.feature.internal.Validators
import com.ibm.appconfiguration.android.lib.feature.models.Feature
import com.ibm.appconfiguration.android.lib.feature.models.internal.Segment
import com.ibm.appconfiguration.android.lib.feature.models.internal.SegmentRules
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

internal class FeatureHandler {

    private lateinit var collectionId: String
    private lateinit var appContext: Context
    private var featuresUpdateListener: FeaturesUpdateListener? = null
    private var isInitialized = false
    private lateinit var urlBuilder: URLBuilder
    private var featureMap: HashMap<String, Feature> = HashMap()
    private var segmentMap: HashMap<String, Segment> = HashMap()
    private var metering: Metering? = null
    private var retryCount = 3

    companion object Factory {
        private var instance: FeatureHandler? = null
        fun getInstance(): FeatureHandler {
            if (instance == null)
                instance =
                    FeatureHandler()
            return instance!!
        }
    }

    fun init(context: Context, collectionId: String) {
        appContext = context
        this.collectionId = collectionId
        urlBuilder = URLBuilder.getInstance()
        urlBuilder.init(collectionId)
        featureMap = HashMap()
        segmentMap = HashMap()
        this.metering = Metering.getInstance()
        this.metering?.init(context)
        this.isInitialized = true
    }

    fun registerFeaturesUpdateListener(listener: FeaturesUpdateListener) {
        if (isInitialized) {
            featuresUpdateListener = listener
        } else {
            Logger.error("Invalid action in FeatureHandler. This action can be performed only after a successful initialization. Please check the initialization section for errors.")
        }
    }

    fun getFeatures(): HashMap<String, Feature>? {
        return featureMap
    }

    fun getFeature(featureId: String?): Feature? {
        return if (featureMap.containsKey(featureId)) {
            featureMap[featureId]
        } else {
            loadFeatures()
            if (featureMap.containsKey(featureId)) {
                featureMap[featureId]
            } else {
                null
            }
        }
    }

    fun fetchFeaturesData() {
        if (this.isInitialized) {
            loadFeatures()
            fetchFromAPI()
            retryCount = 3
        }
    }
    private fun loadFeatures() {
        val allFeature = FileManager.getFileData(appContext) ?: return
        if (allFeature.has("features")) {
            try {
                val allFeatureList = allFeature.optJSONArray("features") ?: return
                for(i in 0 until allFeatureList.length()) {
                    val feature: JSONObject = allFeatureList.getJSONObject(i)
                    val featureObj = Feature(feature)
                    featureMap[featureObj.getFeatureId()] = featureObj
                }
            } catch (e: JSONException) {
                Logger.error("${e.message}")
            }
        }
        if (allFeature.has("segments")) {
            try {
                val segmentList = allFeature.optJSONArray("segments") ?: return
                for(i in 0 until segmentList.length()) {
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

    fun recordEvaluation(featureId: String) {
        metering?.addMetering(
            AppConfiguration.getInstance().getGuid(),
            collectionId,
            featureId
        )
    }

    fun featureEvaluation(feature: Feature, identityAttributes: JSONObject): Any? {

        if (identityAttributes.length() <= 0) {
            return feature.enabled_value
        }

        val rulesMap = parseRules(feature.getSegmentRules())

        for (i in 1..rulesMap.size) {
            val segmentRule = rulesMap[i]
            if (segmentRule != null) {
                for (level in 0 until segmentRule.getRules().length()) {
                    try {
                        val rule = segmentRule.getRules().getJSONObject(level)
                        val segments = rule.getJSONArray("segments")

                        for(innerLevel in 0 until segments.length()) {

                            val segmentKey = segments.getString(innerLevel)
                            if (evaluateSegment(segmentKey, identityAttributes)) {
                                return if (segmentRule.getValue() === "\$default") {
                                    feature.enabled_value
                                } else {
                                    segmentRule.getValue()
                                }
                            }
                        }
                    } catch (e: JSONException) {
                        Logger.error("${ e.message}")
                    }
                }
            }
        }
        return feature.enabled_value
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
        loadFeatures()
        if (featuresUpdateListener != null) {
            featuresUpdateListener?.onFeaturesUpdate()
        }
    }

    private fun fetchFromAPI() {
        if (isInitialized) {

            val configURL = urlBuilder.getConfigUrl()
            val apiManager: APIManager =
                APIManager.newInstance(appContext, configURL, BaseRequest.GET)
            apiManager.setResponseListener( object : ResponseListener {
                override fun onSuccess(response: Response) {
                    var responseText: JSONObject? = null
                    try {
                        responseText = JSONObject(response.getResponseText() ?: "")
                    } catch (e: JSONException) {
                        Logger.error("Error decoding the server response. ${ e.message }")
                    }
                    responseText?.let { writeToFile(it) }
                }

                override fun onFailure(response: Response?, t: Throwable?, extendedInfo: JSONObject?) {
                    Logger.error("Error while fetching the data from server. ${extendedInfo.toString()}")
                }

            })
            apiManager.execute()
        } else {
            Logger.error("fetchFromAPI() - Feature SDK not initialized with call to initialize()")
        }
    }
}