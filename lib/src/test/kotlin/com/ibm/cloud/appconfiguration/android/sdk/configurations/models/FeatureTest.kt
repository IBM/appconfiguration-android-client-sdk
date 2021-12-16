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

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class FeatureTest {

    var sut: Feature? = null

    fun setUpFeature(
        type: ConfigurationType,
        format: String?,
        disabled: Any?,
        enabled: Any?,
        isEnabled: Boolean?
    ) {
        val feature = JSONObject()
        try {
            feature.put("name", "defaultFeature")
            feature.put("feature_id", "defaultfeature")
            feature.put("type", type.toString())
            feature.put("format", format)
            feature.put("disabled_value", disabled)
            feature.put("enabled_value", enabled)
            feature.put("enabled", isEnabled)
            feature.put("segment_exists", false)
            feature.put("segment_rules", JSONArray())
        } catch (e: Exception) {
            println(e)
        }
        sut = Feature(feature)
    }

    @Test
    fun testStringFeature() {
        setUpFeature(ConfigurationType.STRING, "TEXT", "unknown user", "Org user", true)
        assertEquals(sut!!.getFeatureDataType(), ConfigurationType.STRING)
        assertEquals(sut!!.getFeatureDataFormat(), "TEXT")
        assertEquals(sut!!.getFeatureName(), "defaultFeature")
        assertEquals(sut!!.getFeatureId(), "defaultfeature")
        assertEquals(sut!!.isEnabled(), true)
        assertEquals(sut!!.getCurrentValue("pqrt", JSONObject()) as String?, "Org user")
        assertEquals(sut!!.getFeatureDisabledValue(), "unknown user")
        assertEquals(sut!!.getFeatureEnabledValue(), "Org user")
    }

    @Test
    fun testJsonFeature() {
        val enabled = JSONObject()
        enabled.put("name", "tester")
        enabled.put("description", "testing")

        val disabled = JSONObject()
        disabled.put("name", "developer")
        disabled.put("description", "coding")

        setUpFeature(ConfigurationType.STRING, "JSON", disabled, enabled, true)
        assertEquals(sut!!.getFeatureDataType(), ConfigurationType.STRING)
        assertEquals(sut!!.getFeatureDataFormat(), "JSON")
        assertEquals(sut!!.getFeatureName(), "defaultFeature")
        assertEquals(sut!!.getFeatureId(), "defaultfeature")
        assertEquals(sut!!.isEnabled(), true)
        assertEquals(
            (sut!!.getCurrentValue("pqrt", JSONObject()) as JSONObject).get("name"),
            "tester"
        )
        assertEquals((sut!!.getFeatureDisabledValue() as JSONObject).get("name"), "developer")
        assertEquals((sut!!.getFeatureEnabledValue() as JSONObject).get("name"), "tester")
    }

    @Test
    fun testYamlFeature() {
        val enabled =
            "name: tester\ndescription: testing\n---\nname: developer\ndescription: coding"
        val disabled = "name: devops\ndescription: deploying\n"
        setUpFeature(ConfigurationType.STRING, "YAML", disabled, enabled, true)
        assertEquals(sut!!.getFeatureDataType(), ConfigurationType.STRING)
        assertEquals(sut!!.getFeatureDataFormat(), "YAML")
        assertEquals(sut!!.getFeatureName(), "defaultFeature")
        assertEquals(sut!!.getFeatureId(), "defaultfeature")
        assertEquals(sut!!.isEnabled(), true)
        assertEquals(sut!!.getCurrentValue("pqrt", JSONObject()) as String?, enabled)
        assertEquals(sut!!.getFeatureDisabledValue(), disabled)
        assertEquals(sut!!.getFeatureEnabledValue(), enabled)
    }

    @Test
    fun testBooleanFeature() {
        setUpFeature(ConfigurationType.BOOLEAN, "", false, true, true)
        assertEquals(sut!!.getFeatureDataType(), ConfigurationType.BOOLEAN)
        assertEquals(sut!!.getFeatureDataFormat(), "")
        assertEquals(sut!!.getFeatureName(), "defaultFeature")
        assertEquals(sut!!.getFeatureId(), "defaultfeature")
        assertEquals(sut!!.isEnabled(), true)
        assertEquals(sut!!.getCurrentValue("pqrt", JSONObject()) as Boolean?, true)
        assertNull(sut!!.getCurrentValue("", JSONObject()))

    }

    @Test
    fun testNumericFeature() {
        setUpFeature(ConfigurationType.NUMERIC, "", 20, 50, false)
        assertEquals(sut!!.getFeatureDataType(), ConfigurationType.NUMERIC)
        assertEquals(sut!!.getFeatureDataFormat(), "")
        assertEquals(sut!!.getFeatureName(), "defaultFeature")
        assertEquals(sut!!.getFeatureId(), "defaultfeature")
        assertEquals(sut!!.isEnabled(), false)
        assertEquals(sut!!.getCurrentValue("pqrt", JSONObject()), 20)
    }

    @Test
    fun testEmptyJson() {
        sut = Feature(JSONObject())
        assert(sut!!.getFeatureName() == "")
    }
}