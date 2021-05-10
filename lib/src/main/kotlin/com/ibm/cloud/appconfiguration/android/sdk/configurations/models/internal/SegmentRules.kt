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
import org.json.JSONObject

/**
 * SegmentRules object.
 * @param segmentRules JSON object that contains all the SegmentRules.
 */
class SegmentRules(segmentRules: JSONObject) {

    private var order = 1
    private lateinit var value: Any
    private lateinit var rules: JSONArray

    init {
        try {
            order = segmentRules.getInt("order")
            value = segmentRules["value"]
            rules = segmentRules.getJSONArray("rules")
        } catch (e: Exception) {
            Logger.error("Invalid action in SegmentRules class.")
        }
    }

    /**
     * Get the SegmentRules order
     * @return Integer
     */
    fun getOrder(): Int = order

    /**
     * Get the SegmentRules rules array
     * @return JSONArray
     */
    fun getRules(): JSONArray = rules

    /**
     * Get the SegmentRules value
     * @return Any
     */
    fun getValue(): Any = value

}