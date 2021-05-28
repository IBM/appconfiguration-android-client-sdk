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

package com.ibm.cloud.appconfiguration.android.sdk.configurations.models

import com.ibm.cloud.appconfiguration.android.sdk.configurations.ConfigurationHandler
import com.ibm.cloud.appconfiguration.android.sdk.configurations.internal.ConfigMessages
import com.ibm.cloud.appconfiguration.android.sdk.core.Logger
import org.json.JSONArray
import org.json.JSONObject

/**
 * Feature object.
 * @param featureList JSON object that contains all the feature values.
 */
class Feature(featureList: JSONObject) {

    private var enabled = false
    private var name = ""
    private var featureId = ""
    private var segmentRules = JSONArray()
    private var featureData = JSONObject()
    private var type: ConfigurationType = ConfigurationType.NUMERIC
    lateinit var disabledValue: Any
    lateinit var enabledValue: Any

    init {
        try {
            enabled = featureList.getBoolean("enabled")
            name = featureList.getString("name")
            featureId = featureList.getString("feature_id")
            segmentRules = featureList.getJSONArray("segment_rules")
            featureData = featureList
            type = ConfigurationType.valueOf(featureList.getString("type"))
            enabledValue = featureList["enabled_value"]
            disabledValue = featureList["disabled_value"]
        } catch (e: Exception) {
            Logger.error("Invalid action in Feature class. ${e.message}")
        }
    }

    /** Get the Feature name */
    fun getFeatureName(): String = name

    /** Get the Feature ID */
    fun getFeatureId(): String = featureId

    /** Get the Feature dataType */
    fun getFeatureDataType(): ConfigurationType = type

    /** Get current status of the Feature */
    fun isEnabled(): Boolean = enabled

    /** Get segment rules from the Feature */
    fun getSegmentRules(): JSONArray = segmentRules

    fun getFeatureEnabledValue(): Any? {
        return enabledValue
    }

    fun getFeatureDisabledValue(): Any? {
        return disabledValue
    }

    /** Get current value of the Feature. Pass the Data type. */
    fun getCurrentValue(entityId: String, entityAttributes: JSONObject = JSONObject()): Any? {

        if(entityId == "") {
            Logger.error(ConfigMessages.ENTITY_UPDATE_ERROR)
            return null
        }

        val configurationHandler: ConfigurationHandler = ConfigurationHandler.getInstance()
        return configurationHandler.featureEvaluation(this, entityId, entityAttributes)
    }
}