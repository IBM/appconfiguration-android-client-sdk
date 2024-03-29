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
 * Segment object.
 * @param segments JSON object that contains all the Segments.
 */
class Segment(segments: JSONObject) {

    lateinit var name: String
    lateinit var segment_id: String
    lateinit var rules: JSONArray

    init {
        try {
            name = segments.getString("name")
            this.segment_id = segments.getString("segment_id")
            this.rules = segments.getJSONArray("rules")
        } catch (e: Exception) {
            Logger.error("Invalid action in Segment class.")
        }
    }

    /**
     * Method to evaluate the Feature based on the rules
     * @param entityAttributes A JSONObject containing all the user attributes.
     * @return Boolean
     */
    fun evaluateRule(entityAttributes: JSONObject?): Boolean {
        for (index in 0 until rules.length()) {
            try {
                val rule =
                    Rule(
                        rules.getJSONObject(index)
                    )

                if (!rule.evaluateRule(entityAttributes)) {
                    return false
                }
            } catch (e: JSONException) {
                Logger.error("Invalid action in Segment class. ${e.message}")
            }
        }
        return true
    }
}