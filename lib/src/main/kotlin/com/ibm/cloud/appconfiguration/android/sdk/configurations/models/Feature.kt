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
import com.ibm.cloud.appconfiguration.android.sdk.configurations.internal.ConfigConstants
import com.ibm.cloud.appconfiguration.android.sdk.configurations.internal.ConfigMessages
import com.ibm.cloud.appconfiguration.android.sdk.configurations.internal.Validators
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
    private var format: String? = ""
    private lateinit var disabledValue: Any
    private lateinit var enabledValue: Any
    private var rolloutPercentage: Int = ConfigConstants.DEFAULT_ROLLOUT_PERCENTAGE

    init {
        try {
            enabled = featureList.getBoolean("enabled")
            name = featureList.getString("name")
            featureId = featureList.getString("feature_id")
            segmentRules = featureList.getJSONArray("segment_rules")
            featureData = featureList
            type = ConfigurationType.valueOf(featureList.getString("type"))
            format = featureList.optString("format")
            enabledValue = featureList["enabled_value"]
            disabledValue = featureList["disabled_value"]
            if (featureList.has(ConfigConstants.ROLLOUT_PERCENTAGE)) {
                    rolloutPercentage = featureList.getInt(ConfigConstants.ROLLOUT_PERCENTAGE)
            }
        } catch (e: Exception) {
            Logger.error("Invalid action in Feature class. ${e.message}")
        }
    }

    /**
     * Get the Feature name.
     *
     * @return the feature name
     */
    fun getFeatureName(): String = name

    /**
     * Get the Feature Id.
     *
     * @return the feature id
     */
    fun getFeatureId(): String = featureId

    /**
     * Get the feature data type.
     *
     * @return string named BOOLEAN/STRING/NUMERIC
     */
    fun getFeatureDataType(): ConfigurationType = type

    /**
     * Get the feature data format.
     * Applicable only for [ConfigurationType.STRING]
     *
     * @return string named TEXT/JSON/YAML
     */
    fun getFeatureDataFormat(): String? {
        // Format will be empty string ("") for Boolean & Numeric feature flags
        // If the Format is empty for a String type, we default it to TEXT
        if (format == "" && type == ConfigurationType.STRING) {
            format = "TEXT"
        }
        return format
    }

    /**
     * Return the state of the feature flag. Returns true, if the feature flag is enabled, otherwise returns false.
     *
     * @return `true` or `false`
     */
    fun isEnabled(): Boolean {
        return enabled
    }

    /**
     * Get the rules of the Segment targeted.
     *
     * @return segment rules
     */
    fun getSegmentRules(): JSONArray = segmentRules

    /**
     * Get the enabled value of the feature.
     *
     * @return enabled value
     */
    fun getFeatureEnabledValue(): Any {
        return enabledValue
    }

    /**
     * Get the disabled value of the feature.
     *
     * @return disabled value
     */
    fun getFeatureDisabledValue(): Any {
        return disabledValue
    }

    /**
     * Get the rollout_percentage
     *
     * @return rollout_percentage
     */
    fun getRolloutPercentage(): Int {
        return rolloutPercentage
    }

    /**
     * @param entityId Id of the Entity.
     * This will be a string identifier related to the Entity against which the feature is evaluated.
     * For example, an entity might be an instance of an app that runs on a mobile device, a microservice that runs on the cloud, or a component of infrastructure that runs that microservice.
     * For any entity to interact with App Configuration, it must provide a unique entity ID.
     *
     * @param entityAttributes A JSON object consisting of the attribute name and their values that defines the specified entity.
     * This is an optional parameter if the feature flag is not configured with any targeting definition. If the targeting is configured,
     * then entityAttributes should be provided for the rule evaluation.
     * An attribute is a parameter that is used to define a segment. The SDK uses the attribute values to determine if the
     * specified entity satisfies the targeting rules, and returns the appropriate feature flag value.
     *
     * @return {boolean|string|number|null} Returns one of the Enabled/Disabled/Overridden value
     * based on the evaluation.
     * The data type of returned value matches that of feature flag.
     */
    fun getCurrentValue(entityId: String, entityAttributes: JSONObject? = JSONObject()): Any? {

        if (entityId == "") {
            Logger.error(ConfigMessages.ENTITY_UPDATE_ERROR)
            return null
        }

        val configurationHandler: ConfigurationHandler = ConfigurationHandler.getInstance()
        val map: HashMap<String, Any?> = configurationHandler.featureEvaluation(
            this, entityId, entityAttributes) as HashMap<String, Any?>
        return map[ConfigConstants.CURRENT_VALUE]
    }

    /**
     * Get the evaluated value of the feature.
     *
     * @param entityId id of the entity
     * This will be a string identifier related to the Entity against which the feature is evaluated.
     * For example, an entity might be an instance of an app that runs on a mobile device or a
     * microservice that runs on the cloud,
     * or a component of infrastructure that runs that microservice.
     * For any entity to interact with App Configuration, it must provide a unique entity ID.
     * @return {boolean|string|number|null} Returns one of the Enabled/Disabled/Overridden value based on the evaluation.
     * The data type of returned value matches that of feature flag.
     */
    fun getCurrentValue(entityId: String): Any? {
        return getCurrentValue(entityId, null)
    }

}