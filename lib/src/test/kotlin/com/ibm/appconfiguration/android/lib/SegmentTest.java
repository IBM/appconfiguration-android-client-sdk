/*
 * (C) Copyright IBM Corp. 2021.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.ibm.appconfiguration.android.lib;

import com.ibm.appconfiguration.android.lib.feature.models.internal.Segment;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SegmentTest {

    Segment sut;

    public void setUp() {
        JSONObject segment = new JSONObject();

        try {

            JSONObject rules1 = new JSONObject();
            JSONArray values1 = new JSONArray();
            values1.put("100");
            JSONObject rules2 = new JSONObject();
            JSONArray values2 = new JSONArray();
            values2.put("50");

            rules1.put("values",values1);
            rules1.put("operator","lesserThanEquals");
            rules1.put("attribute_name","radius");

            rules2.put("values",values2);
            rules2.put("operator","lesserThan");
            rules2.put("attribute_name","cityRadius");

            JSONArray rules = new JSONArray();
            rules.put(rules1);
            rules.put(rules2);

            segment.put("name","RegionalUser");
            segment.put("segment_id","kdu77n4s");
            segment.put("rules",rules);

        } catch (Exception e) {
            System.out.println(e);
        }
        this.sut = new Segment(segment);
    }

    @Test
    public void testSegment() {
        setUp();
        JSONObject clientAttributes = new JSONObject();
        try {
            clientAttributes.put("radius","100");
            clientAttributes.put("cityRadius","35");
            assertTrue(sut.evaluateRule(clientAttributes));
        } catch (Exception e) {
            System.out.println(e);
            assertFalse(true);
        }

    }
}