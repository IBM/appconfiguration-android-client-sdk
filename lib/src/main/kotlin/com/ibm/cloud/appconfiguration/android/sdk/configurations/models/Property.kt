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
 * Property object.
 * @param propertyList JSON object that contains all the Property values.
 */
class Property(propertyList: JSONObject) {

    private var name = ""
    private var propertyId = ""
    private var segmentRules = JSONArray()
    private var type: ConfigurationType = ConfigurationType.NUMERIC
    private var value: Any = ""

    init {
        try {
            name = propertyList.getString("name")
            propertyId = propertyList.getString("property_id")
            segmentRules = propertyList.getJSONArray("segment_rules")
            type = ConfigurationType.valueOf(propertyList.getString("type"))
            value = propertyList["value"]
        } catch (e: Exception) {
            Logger.error("Invalid action in Property class. ${e.message}")
        }
    }

    /**
     * Get the Property name.
     *
     * @return property name
     */
    fun getPropertyName(): String = name

    /**
     * Get the Property Id.
     *
     * @return property id
     */
    fun getPropertyId(): String = propertyId

    /**
     * Get the Property data type.
     *
     * @return string named BOOLEAN/STRING/NUMERIC
     */
    fun getPropertyDataType(): ConfigurationType = type

    /**
     * Get the rules of the Segment targeted.
     *
     * @return segment rules JSON
     */
    fun getSegmentRules(): JSONArray = segmentRules

    /**
     * Get the default property value.
     *
     * @return default property value
     */
    fun getPropertyValue(): Any {
        return value
    }

    /**
     * Get the evaluated value of the property. Pass the Data type
     *
     * @param entityId id of the entity
     * @param entityAttributes entity attributes JSON object
     * @return evaluated value
     */
    fun getCurrentValue(entityId: String, entityAttributes: JSONObject = JSONObject()): Any? {

        if (entityId == "") {
            Logger.error(ConfigMessages.ENTITY_UPDATE_ERROR)
            return null
        }

        val configurationHandler: ConfigurationHandler = ConfigurationHandler.getInstance()
        return configurationHandler.propertyEvaluation(this, entityId, entityAttributes)
    }
}