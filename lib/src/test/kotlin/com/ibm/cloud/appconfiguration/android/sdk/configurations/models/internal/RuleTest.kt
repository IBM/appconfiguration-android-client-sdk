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
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import org.junit.Assert.assertEquals

class RuleTest {

    var sut: Rule? = null

    fun setUpEndsWith() {
        val rules = JSONObject()
        val values = JSONArray()
        values.put("ibm.com")
        try {
            rules.put("values", values)
            rules.put("operator", "endsWith")
            rules.put("attribute_name", "email")
        } catch (e: Exception) {
            println(e)
        }
        sut = Rule(rules)
    }

    fun setUpEquals(value: Any?) {
        val rules = JSONObject()
        val values = JSONArray()
        values.put(value)
        try {
            rules.put("values", values)
            rules.put("operator", "is")
            rules.put("attribute_name", "creditValues")
        } catch (e: Exception) {
            println(e)
        }
        sut = Rule(rules)
    }

    @Test
    @Throws(JSONException::class)
    fun testRules() {
        setUpEndsWith()
        assertEquals(sut!!.attribute_name, "email")
        assertEquals(sut!!.operator, "endsWith")
        assertEquals(sut!!.values.length(), 1)
        assertEquals(sut!!.values.getString(0), "ibm.com")
    }

    @Test
    fun TestEvaluationEndsWithString() {
        setUpEndsWith()
        val clientAttributes = JSONObject()
        try {
            clientAttributes.put("email", "tester@ibm.com")
            Assert.assertTrue(sut!!.evaluateRule(clientAttributes))
            clientAttributes.put("email", "tester@ibm.error")
            Assert.assertFalse(sut!!.evaluateRule(clientAttributes))
        } catch (e: Exception) {
            println(e)
        }
    }

    @Test
    fun TestEvaluationEndsWithDifferentValues() {
        val clientAttributes = JSONObject()
        try {
            clientAttributes.put("creditValues", "123")
            setUpEquals("123")
            Assert.assertTrue(sut!!.evaluateRule(clientAttributes))
            clientAttributes.put("creditValues", "false")
            setUpEquals("false")
            Assert.assertTrue(sut!!.evaluateRule(clientAttributes))
            clientAttributes.put("creditValues", 123)
            setUpEquals("123")
            Assert.assertTrue(sut!!.evaluateRule(clientAttributes))
            setUpEquals(123)
            Assert.assertTrue(sut!!.evaluateRule(clientAttributes))
            clientAttributes.put("creditValues", false)
            setUpEquals("123")
            Assert.assertFalse(sut!!.evaluateRule(clientAttributes))
            setUpEquals(123)
            Assert.assertFalse(sut!!.evaluateRule(clientAttributes))
            clientAttributes.put("creditValues", false)
            setUpEquals("false")
            Assert.assertFalse(sut!!.evaluateRule(clientAttributes))
            setUpEquals(false)
            Assert.assertTrue(sut!!.evaluateRule(clientAttributes))
        } catch (e: Exception) {
            println(e)
        }
    }
}