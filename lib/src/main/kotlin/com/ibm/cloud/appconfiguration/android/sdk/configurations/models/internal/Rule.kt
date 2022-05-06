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

package com.ibm.cloud.appconfiguration.android.sdk.configurations.models.internal

import com.ibm.cloud.appconfiguration.android.sdk.core.Logger
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


/**
 * Rule object.
 * @param rules JSON object that contains all the Rules.
 */
class Rule(rules: JSONObject) {

    lateinit var attribute_name: String
    lateinit var operator: String
    lateinit var values: JSONArray

    init {
        try {
            attribute_name = rules.getString("attribute_name")
            operator = rules.getString("operator")
            values = rules.getJSONArray("values")
        } catch (e: Exception) {
            Logger.error("Invalid action in Rule class.")
        }
    }

    private fun operatorCheck(keyData: Any?, valueData: Any?): Boolean {

        var key = keyData
        var value = valueData

        var result = false

        if (key == null || value == null) {
            return result
        }

        when (this.operator) {
            "endsWith" -> result = key.toString().endsWith(value.toString())
            "startsWith" -> result = key.toString().startsWith(value.toString())
            "contains" -> result = key.toString().contains(value.toString())
            "is" -> {
                if (key::class == value::class) {
                    result = key == value
                } else {
                    try {
                        result = value.toString() == key.toString()
                    } catch (error: java.lang.Exception) {
                        error.localizedMessage?.let { Logger.error(it) }
                    }
                }
            }
            "greaterThan" -> {
                if (key is Number && value is Number) {
                    result = key.toFloat() > value.toFloat()
                } else if (key is String || value is String) {
                    result = key.toString().toFloat() > value.toString().toFloat()
                }
            }
            "lesserThan" -> {
                if (key is Number && value is Number) {
                    result = key.toFloat() < value.toFloat()
                } else if (key is String || value is String) {
                    result = key.toString().toFloat() < value.toString().toFloat()
                }
            }
            "greaterThanEquals" -> {
                if (key is Number && value is Number) {
                    result = key.toFloat() >= value.toFloat()
                } else if (key is String || value is String) {
                    result = key.toString().toFloat() >= value.toString().toFloat()
                }
            }
            "lesserThanEquals" -> {
                if (key is Number && value is Number) {
                    result = key.toFloat() <= value.toFloat()
                } else if (key is String || value is String) {
                    result = key.toString().toFloat() <= value.toString().toFloat()
                }
            }
            else -> {
                result = false
            }
        }
        return result
    }

    /**
     * Method to evaluate the `Feature` and `Property` based on the rules.
     *
     * @param entityAttributes a JSONObject containing all the user attributes
     * @return `true` if evaluation is passed against respective operator. `false` otherwise
     */
    fun evaluateRule(entityAttributes: JSONObject?): Boolean {
        var key: Any? = null
        var result = false

        if (entityAttributes != null && entityAttributes.has(attribute_name)) {
            try {
                key = entityAttributes[attribute_name]
            } catch (e: JSONException) {
                Logger.error("${e.message}")
            }
        } else {
            return result
        }

        for (i in 0 until values.length()) {
            try {
                val value = values[i]
                if (operatorCheck(key, value)) {
                    result = true
                }
            } catch (e: JSONException) {
                Logger.error("${e.message}")
            }
        }
        return result
    }
}