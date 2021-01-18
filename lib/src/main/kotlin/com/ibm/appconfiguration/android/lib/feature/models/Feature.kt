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

package com.ibm.appconfiguration.android.lib.feature.models

import com.ibm.appconfiguration.android.lib.core.Logger
import com.ibm.appconfiguration.android.lib.feature.FeatureHandler
import com.ibm.appconfiguration.android.lib.feature.internal.Validators
import org.json.JSONArray
import org.json.JSONObject

/**
 * Feature object.
 * @param featureList JSON object that contains all the feature values.
 */
class Feature(featureList: JSONObject) {

    private var enabled = false
    private var name = ""
    private var feature_id = ""
    private var segment_rules = JSONArray()
    private var featureData = JSONObject()
    private var segment_exists = false
    private var type: FeatureType = FeatureType.NUMERIC
    lateinit var disabled_value: Any
    lateinit var enabled_value: Any

    /** Enum to identify the Feature value types */
    enum class FeatureType {
        NUMERIC, STRING, BOOLEAN
    }

    init {
        try {
            enabled = featureList.getBoolean("isEnabled")
            name = featureList.getString("name")
            feature_id = featureList.getString("feature_id")
            segment_rules = featureList.getJSONArray("segment_rules")
            segment_exists = featureList.getBoolean("segment_exists")
            featureData = featureList
            type = FeatureType.valueOf(featureList.getString("type"))
            enabled_value = featureList["enabled_value"]
            disabled_value = featureList["disabled_value"]
        } catch (e: Exception) {
            Logger.error("Invalid action in Feature class. ${e.message}")
        }
    }

    /** Get the Feature name */
    fun getFeatureName(): String = name

    /** Get the Feature ID */
    fun getFeatureId(): String = feature_id

    /** Get the Feature dataType */
    fun getFeatureDataType(): FeatureType = type

    /** Get current status of the Feature */
    fun isEnabled(): Boolean = enabled

    /** Get segment rules from the Feature */
    fun getSegmentRules(): JSONArray = segment_rules

    /** Get current value of the Feature. Pass the Data type. */
    fun <T> getCurrentValue(): T? {
        val featureHandler: FeatureHandler = FeatureHandler.getInstance()
        featureHandler.recordEvaluation(this.feature_id)
        return if (enabled) {
            if (segment_exists && segment_rules.length() > 0) {
                featureHandler.featureEvaluation(this)
            } else {
                Validators.convertValue(enabled_value)
            }
        } else {
            Validators.convertValue(disabled_value)
        }
    }
}