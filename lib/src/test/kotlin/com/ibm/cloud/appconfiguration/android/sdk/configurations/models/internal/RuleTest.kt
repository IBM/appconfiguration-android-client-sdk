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

import com.ibm.cloud.appconfiguration.android.sdk.configurations.models.Feature
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import kotlin.isInitialized as isInitialized1

class RuleTest {

    var sut: Rule? = null

    private fun setUpRuleObject(operator: Any = "endsWith", value: Any = "dev.com", attribute_name: String = "email") {
        val rules = JSONObject()
        val values = JSONArray()
        values.put(value)
        try {
            rules.put("values", values)
            rules.put("operator", operator)
            rules.put("attribute_name", attribute_name)
        } catch (e: Exception) {
            println(e)
        }
        sut = Rule(rules)
    }

    @Test
    fun testRules() {
        setUpRuleObject()
        assertEquals(sut!!.attribute_name, "email")
        assertEquals(sut!!.operator, "endsWith")
        assertEquals(sut!!.values.length(), 1)
        assertEquals(sut!!.values.getString(0), "dev.com")
    }

    @Test
    fun testEvaluationEndsWithString() {
        setUpRuleObject()
        val clientAttributes = JSONObject()
        try {
            clientAttributes.put("email", "tester@dev.com")
            assertTrue(sut!!.evaluateRule(clientAttributes))
            clientAttributes.put("email", "tester@dev.error")
            assertFalse(sut!!.evaluateRule(clientAttributes))
        } catch (e: Exception) {
            println(e)
        }
    }

    @Test
    fun testEmptyJson() {
        sut = Rule(JSONObject())
    }

    @Test
    fun testEvaluationEndsWithDifferentValues() {
        val clientAttributes = JSONObject()
        try {

            clientAttributes.put("email", "tester@dev")
            setUpRuleObject("startsWith","tester")
            assertTrue(sut!!.evaluateRule(clientAttributes))

            clientAttributes.put("creditValues", 123)
            setUpRuleObject("is",123, "creditValues")
            assertTrue(sut!!.evaluateRule(clientAttributes))

            clientAttributes.put("creditValues", 123)
            setUpRuleObject("is","123", "creditValues")
            assertTrue(sut!!.evaluateRule(clientAttributes))

            clientAttributes.put("creditValues", 130)
            setUpRuleObject("greaterThan",120, "creditValues")
            assertTrue(sut!!.evaluateRule(clientAttributes))

            clientAttributes.put("creditValues", "130")
            setUpRuleObject("greaterThan",120, "creditValues")
            assertTrue(sut!!.evaluateRule(clientAttributes))

            clientAttributes.put("creditValues", 120)
            setUpRuleObject("lesserThan",123, "creditValues")
            assertTrue(sut!!.evaluateRule(clientAttributes))

            clientAttributes.put("creditValues", 120)
            setUpRuleObject("lesserThan","123", "creditValues")
            assertTrue(sut!!.evaluateRule(clientAttributes))

            clientAttributes.put("creditValues", 123)
            setUpRuleObject("greaterThanEquals",120, "creditValues")
            assertTrue(sut!!.evaluateRule(clientAttributes))

            clientAttributes.put("creditValues", 123)
            setUpRuleObject("greaterThanEquals","120", "creditValues")
            assertTrue(sut!!.evaluateRule(clientAttributes))

            clientAttributes.put("creditValues", 120)
            setUpRuleObject("lesserThanEquals",120, "creditValues")
            assertTrue(sut!!.evaluateRule(clientAttributes))

            clientAttributes.put("creditValues", "120")
            setUpRuleObject("lesserThanEquals",120, "creditValues")
            assertTrue(sut!!.evaluateRule(clientAttributes))

            clientAttributes.put("creditValues", 120)
            setUpRuleObject("none",120, "creditValues")
            assertFalse(sut!!.evaluateRule(clientAttributes))


            clientAttributes.put("creditValues", JSONObject())
            setUpRuleObject("is",120, "creditValues")
            assertFalse(sut!!.evaluateRule(clientAttributes))


        } catch (e: Exception) {
            println(e)
        }
    }
}