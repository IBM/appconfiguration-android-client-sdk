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

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test

class SegmentTest {

    var sut: Segment? = null

    fun setUp() {
        val segment = JSONObject()
        try {
            val rules1 = JSONObject()
            val values1 = JSONArray()
            values1.put("100")
            val rules2 = JSONObject()
            val values2 = JSONArray()
            values2.put("50")
            rules1.put("values", values1)
            rules1.put("operator", "lesserThanEquals")
            rules1.put("attribute_name", "radius")
            rules2.put("values", values2)
            rules2.put("operator", "lesserThan")
            rules2.put("attribute_name", "cityRadius")
            val rules = JSONArray()
            rules.put(rules1)
            rules.put(rules2)
            segment.put("name", "RegionalUser")
            segment.put("segment_id", "kdu77n4s")
            segment.put("rules", rules)
        } catch (e: Exception) {
            println(e)
        }
        sut = Segment(segment)
    }

    @Test
    fun testSegment() {
        setUp()
        val clientAttributes = JSONObject()
        try {
            clientAttributes.put("radius", "100")
            clientAttributes.put("cityRadius", "35")
            Assert.assertTrue(sut!!.evaluateRule(clientAttributes))
        } catch (e: Exception) {
            println(e)
            Assert.assertFalse(true)
        }
    }
}