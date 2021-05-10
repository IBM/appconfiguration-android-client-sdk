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

import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Test


class MeteringTest {
    @Test
    public fun testStringMetering(){
        val metering = Metering.getInstance()

        metering.addMetering(
            "guid1",
            "environment_id1",
            "collectionId1",
            "identityId1",
            "segmentId1",
            "featureId1",
            null
        )
        metering.addMetering(
            "guid2",
            "environment_id1",
            "collectionId2",
            "identityId1",
            "segmentId1",
            "featureId1",
            null
        )
        metering.addMetering(
            "guid1",
            "environment_id1",
            "collectionId2",
            "identityId1",
            "segmentId1",
            "featureId1",
            null
        )

        metering.addMetering(
            "guid1",
            "environment_id2",
            "collectionId1",
            "identityId1",
            "segmentId1",
            null,
            "property_id1"
        )
        metering.addMetering(
            "guid2",
            "environment_id2",
            "collectionId2",
            "identityId1",
            "segmentId1",
            null,
            "property_id1"
        )
        metering.addMetering(
            "guid1",
            "environment_id2",
            "collectionId2",
            "identityId1",
            "segmentId1",
            null,
            "property_id1"
        )

        metering.addMetering(
            "guid2",
            "environment_id2",
            "collectionId2",
            "identityId1",
            "$\$null$$",
            null,
            "property_id1"
        )

        val result: HashMap<String, JSONArray> = metering.sendMetering()
        print(result)
        assertEquals(4, result["guid1"]!!.length())
    }

}