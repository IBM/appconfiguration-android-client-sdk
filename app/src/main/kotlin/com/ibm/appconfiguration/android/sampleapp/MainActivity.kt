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

package com.ibm.appconfiguration.android.sampleapp

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.ibm.appconfiguration.android.lib.AppConfiguration
import com.ibm.appconfiguration.android.lib.feature.FeaturesUpdateListener
import com.ibm.appconfiguration.android.lib.feature.models.Feature
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    var nDialog: ProgressBar? = null
    var textView: TextView? = null
    var constraintLayout: ConstraintLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        constraintLayout = findViewById(R.id.layoutc)

        val feature1Button = findViewById<Button>(R.id.stringButton)
        val feature2Button = findViewById<Button>(R.id.numberButton)
        val feature3Button = findViewById<Button>(R.id.booleanButton)

        nDialog = findViewById(R.id.progressBar)
        nDialog?.visibility = View.INVISIBLE

        textView = findViewById(R.id.textView)

        setUp()
        val clickListener = View.OnClickListener { view ->

            nDialog?.visibility = View.VISIBLE

            when (view.id) {
                R.id.stringButton -> buttonAction("featurestring")
                R.id.numberButton -> buttonAction("featurenumeric")
                R.id.booleanButton -> buttonAction("featurebool")
            }
        }
        feature1Button.setOnClickListener(clickListener)
        feature2Button.setOnClickListener(clickListener)
        feature3Button.setOnClickListener(clickListener)
    }

    private fun setUp() {

        nDialog?.visibility = View.VISIBLE

        val expectedApiKey = APIKEY
        val expectedGuid = GUID

        val appConfiguration = AppConfiguration.getInstance()
        appConfiguration.init(application, AppConfiguration.REGION_US_SOUTH, expectedGuid, expectedApiKey)
        //appConfiguration.fetchFeatureData()
        val attributes = JSONObject()
        try {
            attributes.put("cityRadius", "40")
            attributes.put("radius", "50")
            appConfiguration.setClientAttributes(attributes)
        } catch (e: JSONException) {
            e.printStackTrace()
        }


        appConfiguration.setCollectionId(COLLECTIONID)
        appConfiguration.setClientAttributes(attributes)
        appConfiguration.enableDebug(true)

        appConfiguration.registerFeaturesUpdateListener(object :
            FeaturesUpdateListener {
            override fun onFeaturesUpdate() {
                nDialog?.visibility = View.INVISIBLE

                println(appConfiguration.getFeature("featurebool"))
                println(appConfiguration.getFeatures())

            }

        })

    }

    private fun buttonAction(featureId : String) {

        nDialog?.visibility = View.INVISIBLE

        val appConfiguration = AppConfiguration.getInstance()
        val feature: Feature? = appConfiguration.getFeature(featureId)
        if (feature?.getFeatureDataType() === Feature.FeatureType.NUMERIC) {
            textView!!.text = featureId + "value is :" + feature.getCurrentValue<Int>()
            constraintLayout!!.setBackgroundColor(Color.RED)
        } else if (feature?.getFeatureDataType() === Feature.FeatureType.BOOLEAN) {
            val value = feature.getCurrentValue<Boolean>()
            println(value)
            textView!!.text = featureId + "value is :" + value
            constraintLayout!!.setBackgroundColor(Color.GREEN)
        } else if (feature?.getFeatureDataType() === Feature.FeatureType.STRING) {
            val value = feature.getCurrentValue<String>()
            println(value)
            textView!!.text = featureId + "value is :" + value
            constraintLayout!!.setBackgroundColor(Color.YELLOW)
        }
    }
}