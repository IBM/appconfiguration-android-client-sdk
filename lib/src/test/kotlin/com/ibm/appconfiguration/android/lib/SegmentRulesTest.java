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

import com.ibm.appconfiguration.android.lib.feature.models.internal.SegmentRules;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class SegmentRulesTest {

    SegmentRules sut;

    public void setUpRules() {

        JSONObject segmentRules = new JSONObject();
        JSONArray rules = new JSONArray();
        JSONArray segments = new JSONArray();
        try {
            segments.put("kg92d3wa");
            JSONObject jsObj = new JSONObject();
            jsObj.put("segments", segments);
            rules.put(jsObj);

            segmentRules.put("rules", rules);
            segmentRules.put("value", "IBM user");
            segmentRules.put("order", 1);

        } catch (Exception e) {
            System.out.println(e);
        }

        this.sut = new SegmentRules(segmentRules);
    }

    @Test
    public void testSegmentRules() {
        setUpRules();
        assertEquals(this.sut.getOrder(), 1);
        assertEquals(this.sut.getValue(), "IBM user");
        assertEquals(this.sut.getValue(), "IBM user");
        assertEquals(this.sut.getRules().length(), 1);

    }


}
