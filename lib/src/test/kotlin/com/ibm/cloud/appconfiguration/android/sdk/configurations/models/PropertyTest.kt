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
import org.junit.Assert.assertEquals
import org.junit.Test


class PropertyTest {

    private lateinit var sut: Property

    private fun setUpStringProperty(type: ConfigurationType, value: Any?) {

        val property = JSONObject()
        try {
            property.put("name", "defaultProperty")
            property.put("property_id", "defaultproperty")
            property.put("type", type.toString())
            property.put("value", value)
            property.put("segment_rules", JSONArray())
        } catch (e: Exception) {
            println(e)
        }
        sut = Property(property)
    }


    @Test
    fun testProperty() {
        setUpStringProperty(ConfigurationType.STRING, "unknown user")
        assertEquals(sut.getPropertyDataType(), ConfigurationType.STRING)
        assertEquals(sut.getPropertyName(), "defaultProperty")
        assertEquals(sut.getPropertyId(), "defaultproperty")
    }

    @Test
    fun testBooleanProperty() {
        setUpStringProperty(ConfigurationType.BOOLEAN, false)
        assertEquals(sut.getPropertyDataType(), ConfigurationType.BOOLEAN)
        assertEquals(sut.getPropertyName(), "defaultProperty")
        assertEquals(sut.getPropertyId(), "defaultproperty")
    }

    @Test
    fun testNumericProperty() {
        setUpStringProperty(ConfigurationType.NUMERIC, 20)
        assertEquals(sut.getPropertyDataType(), ConfigurationType.NUMERIC)
        assertEquals(sut.getPropertyName(), "defaultProperty")
        assertEquals(sut.getPropertyId(), "defaultproperty")
        assertEquals(sut.getCurrentValue("d"), 20)
    }
}