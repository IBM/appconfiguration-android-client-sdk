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

package com.ibm.cloud.sampleapp

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.ibm.cloud.appconfiguration.android.sdk.AppConfiguration
import com.ibm.cloud.appconfiguration.android.sdk.configurations.ConfigurationUpdateListener
import com.ibm.cloud.appconfiguration.android.sdk.configurations.models.ConfigurationType
import com.ibm.cloud.appconfiguration.android.sdk.configurations.models.Feature
import com.ibm.cloud.appconfiguration.android.sdk.configurations.models.Property
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    var nDialog: ProgressBar? = null
    var textView: TextView? = null
    var constraintLayout: ConstraintLayout? = null
    val entityAttributes = JSONObject()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        constraintLayout = findViewById(R.id.layoutc)

        val feature1Button = findViewById<Button>(R.id.stringButton)
        val feature2Button = findViewById<Button>(R.id.numberButton)
        val feature3Button = findViewById<Button>(R.id.booleanButton)
        val propertyButton = findViewById<Button>(R.id.propertyButton)


        nDialog = findViewById(R.id.progressBar)
        nDialog?.visibility = View.INVISIBLE
        textView = findViewById(R.id.textView)

        try {
            entityAttributes.put("cityRadius", "40")
            entityAttributes.put("radius", "50")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        setUp()
        val clickListener = View.OnClickListener { view ->

            nDialog?.visibility = View.VISIBLE

            when (view.id) {
                R.id.stringButton -> buttonAction("featurestring")
                R.id.numberButton -> buttonAction("featurenumeric")
                R.id.booleanButton -> buttonAction("featurebool")
                R.id.propertyButton -> buttonActionProperty("numericproperty")
            }

        }
        feature1Button.setOnClickListener(clickListener)
        feature2Button.setOnClickListener(clickListener)
        feature3Button.setOnClickListener(clickListener)
        propertyButton.setOnClickListener(clickListener)
    }

    private fun setUp() {

        nDialog?.visibility = View.VISIBLE

        val expectedApiKey = getString(R.string.apikey)
        val expectedGuid = getString(R.string.guid)

        val appConfiguration = AppConfiguration.getInstance()
        appConfiguration.init(application, AppConfiguration.REGION_US_SOUTH, expectedGuid, expectedApiKey)
        appConfiguration.setContext(getString(R.string.collectionId),getString(R.string.environmentId))

        appConfiguration.enableDebug(true)

        appConfiguration.registerConfigurationUpdateListener(object : ConfigurationUpdateListener {

            override fun onConfigurationUpdate() {
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
        if (feature?.getFeatureDataType() === ConfigurationType.NUMERIC) {
            textView!!.text = featureId + "value is :" + feature.getCurrentValue("pvqr", entityAttributes)
            constraintLayout!!.setBackgroundColor(Color.RED)
        } else if (feature?.getFeatureDataType() === ConfigurationType.BOOLEAN) {
            val value = feature.getCurrentValue("pvqr", entityAttributes)
            println(value)
            textView!!.text = featureId + "value is :" + value
            constraintLayout!!.setBackgroundColor(Color.GREEN)
        } else if (feature?.getFeatureDataType() === ConfigurationType.STRING) {
            val value = feature.getCurrentValue("pvqr", entityAttributes)
            println(value)
            textView!!.text = featureId + "value is :" + value
            constraintLayout!!.setBackgroundColor(Color.YELLOW)
        }
    }

    private fun buttonActionProperty(propertyId : String) {

        nDialog?.visibility = View.INVISIBLE

        val appConfiguration = AppConfiguration.getInstance()
        val property: Property? = appConfiguration.getProperty(propertyId)

        textView!!.text = propertyId + "value is :" + property!!.getCurrentValue("pvqr", entityAttributes)
        constraintLayout!!.setBackgroundColor(Color.RED)
    }
}