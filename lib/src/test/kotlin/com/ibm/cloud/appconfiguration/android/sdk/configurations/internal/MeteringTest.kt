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

package com.ibm.cloud.appconfiguration.android.sdk.configurations.internal

import android.content.Context
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito


class MeteringTest {


    private var server = MockWebServer()

    @Before
    fun setup() {
        server.start(8080)

        val response = MockResponse().setResponseCode(202)
            .setBody("{\"status\":\"success\"}")
        server.enqueue(response)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun testMeteringSplit() {

        val usages = JSONObject()
        usages.put("feature_id", "featureId1")
        usages.put("evaluation_time", "2021-04-15T11:07:56.013Z")
        usages.put("entity_id", "entityId1")
        usages.put("count", 1)
        usages.put("segment_id", "segmentId1")

        val array = JSONArray()

        for (i in 0..29) {
            array.put(usages)
        }

        val data = JSONObject()

        data.put("collection_id", "collection_id")
        data.put("environment_id", "environment_id")
        data.put("usages", array)

        runBlocking {

            val result: JSONArray = Metering.getInstance().sendSplitMetering("guid", data, 30)
            assertEquals(result.length(), 3)
            assertEquals((result[0] as JSONObject).getJSONArray("usages").length(), 10)
            assertEquals((result[1] as JSONObject).getJSONArray("usages").length(), 10)
            assertEquals((result[2] as JSONObject).getJSONArray("usages").length(), 10)
        }

    }
}
