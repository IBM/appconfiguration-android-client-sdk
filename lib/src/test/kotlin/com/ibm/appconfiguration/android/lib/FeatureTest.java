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

import com.ibm.appconfiguration.android.lib.feature.models.Feature;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FeatureTest {

    Feature sut;

    public void setUpStringFeature(Feature.FeatureType type, Object disabled, Object enaabled, Boolean isEnabled) {

        JSONObject feature = new JSONObject();
        try {
            feature.put("name","defaultFeature");
            feature.put("feature_id","defaultfeature");
            feature.put("type",type.toString());
            feature.put("disabled_value",disabled);
            feature.put("enabled_value",enaabled);
            feature.put("isEnabled",isEnabled);
            feature.put("segment_exists", false);
            feature.put("segment_rules",new JSONArray());

        } catch (Exception e) {
            System.out.println(e);
        }
        this.sut = new Feature(feature);
    }

    @Test
    public void testStringFeature() {
        setUpStringFeature(Feature.FeatureType.STRING, "unknown user","Org user", true );
        assertEquals(sut.getFeatureDataType(), Feature.FeatureType.STRING);
        assertEquals(sut.getFeatureName(), "defaultFeature");
        assertEquals(sut.getFeatureId(), "defaultfeature");
        assertEquals(sut.isEnabled(), true);
        assertEquals((String) sut.getCurrentValue(), "Org user");
    }

    @Test
    public void testBooleanFeature() {
        setUpStringFeature(Feature.FeatureType.BOOLEAN, false,true , true);
        assertEquals(sut.getFeatureDataType(), Feature.FeatureType.BOOLEAN);
        assertEquals(sut.getFeatureName(), "defaultFeature");
        assertEquals(sut.getFeatureId(), "defaultfeature");
        assertEquals(sut.isEnabled(), true);
        assertEquals((Boolean) sut.getCurrentValue(), true);

    }

    @Test
    public void testNumericFeature() {
        setUpStringFeature(Feature.FeatureType.NUMERIC, 20,50, false );
        assertEquals(sut.getFeatureDataType(), Feature.FeatureType.NUMERIC);
        assertEquals(sut.getFeatureName(), "defaultFeature");
        assertEquals(sut.getFeatureId(), "defaultfeature");
        assertEquals(sut.isEnabled(), false);
        assertEquals((int) sut.getCurrentValue(), 20);

    }
}
