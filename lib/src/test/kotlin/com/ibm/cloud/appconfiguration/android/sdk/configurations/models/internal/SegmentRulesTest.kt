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

class SegmentRulesTest {

    var sut: SegmentRules? = null

    fun setUpRules() {
        val segmentRules = JSONObject()
        val rules = JSONArray()
        val segments = JSONArray()
        try {
            segments.put("kg92d3wa")
            val jsObj = JSONObject()
            jsObj.put("segments", segments)
            rules.put(jsObj)
            segmentRules.put("rules", rules)
            segmentRules.put("value", "IBM user")
            segmentRules.put("order", 1)
        } catch (e: Exception) {
            println(e)
        }
        sut = SegmentRules(segmentRules)
    }

    @Test
    fun testSegmentRules() {
        setUpRules()
        Assert.assertEquals(sut!!.getOrder().toLong(), 1)
        Assert.assertEquals(sut!!.getValue(), "IBM user")
        Assert.assertEquals(sut!!.getValue(), "IBM user")
        Assert.assertEquals(sut!!.getRules().length().toLong(), 1)
    }

}