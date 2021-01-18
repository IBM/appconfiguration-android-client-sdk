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

package com.ibm.appconfiguration.android.lib.feature.internal

import android.content.Context
import com.ibm.appconfiguration.android.lib.core.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

internal class Metering {

    private var sendInterval:Long = 5
    private var appContext: Context? = null
    private var urlBuilder: URLBuilder? = null
    var meteringData: ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>> =
        ConcurrentHashMap()

    companion object Factory {
        private var instance: Metering? = null
        fun getInstance(): Metering {
            if (instance == null)
                instance =
                    Metering()
            return instance!!
        }
    }

    fun init(appContext: Context) {
        this.appContext = appContext
    }

    init {
        urlBuilder =
            URLBuilder.getInstance()
        Timer().schedule(
            object : TimerTask() {
                override fun run() {
                    sendMetering()
                }
            },
            5, sendInterval
        )
    }

    fun addMetering(guid: String, collectionId: String, feature: String) {
        var hasData = false
        val featureJson: HashMap<String, Any> = HashMap<String, Any>()
        featureJson["count"] = 1
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val currentDate = simpleDateFormat.format(Date())
        featureJson["evaluation_time"] = currentDate

        if (this.meteringData.contains(guid)) {

            if (meteringData[guid]!!.containsKey(collectionId)) {

                if (meteringData[guid]!![collectionId]!!.containsKey(feature)) {
                    hasData = true
                    meteringData[guid]!![collectionId]!![feature]!!["evaluation_time"] = currentDate
                    val count = meteringData[guid]!![collectionId]!![feature]!!["count"] as Int
                    meteringData[guid]!![collectionId]!![feature]!!["count"] = count + 1
                } else {
                    meteringData[guid]!![collectionId]!![feature] = HashMap()
                }
            } else {
                var featureMap = ConcurrentHashMap<String, HashMap<String, Any>>()
                featureMap[feature] = HashMap()
                this.meteringData[guid]!![collectionId] = featureMap
            }
        } else {

            var featureMap = ConcurrentHashMap<String, HashMap<String, Any>>()
            var collectionIdMap = ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>()
            featureMap[feature] = HashMap()
            collectionIdMap[collectionId] = featureMap
            this.meteringData[guid] = collectionIdMap
        }

        if (!hasData) {
            meteringData[guid]!![collectionId]!![feature]!!.putAll(featureJson)
        }

    }
    private fun sendMetering() {

        if (appContext == null || urlBuilder == null) {
            return
        }
        val sendMeteringData: ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, java.util.HashMap<String, Any>>>> =
            meteringData
        meteringData = ConcurrentHashMap()

        for ( collections in sendMeteringData) {

            val guid: String = collections.key
            val dataToSend = JSONObject()

            for ( collection in collections.value) {

                try {

                    dataToSend.put("collection_id", collection.key)
                    val array = JSONArray()
                    for (feature in collection.value) {
                        val featuresObj: HashMap<String, Any> = feature.value
                        val featureJson = JSONObject()
                        featureJson.put("feature_id", feature.key)
                        featureJson.put("evaluation_time", featuresObj["evaluation_time"])
                        featureJson.put("count", featuresObj["count"])
                        array.put(featureJson)
                    }
                    dataToSend.put("usages", array)
                    val configURL = urlBuilder!!.getMeteringurl(guid) ?: ""
                    val apiManager: APIManager =
                        APIManager.newInstance(appContext!!, configURL, BaseRequest.POST)
                    apiManager.setJSONRequestBody(dataToSend)
                    apiManager.setResponseListener(object : ResponseListener {
                        override fun onSuccess(response: Response) {

                            val status = response.getStatus()
                            if (status in 200..299) {
                                Logger.debug("Successfully pushed the data to metering'")
                            } else {
                                Logger.error("Error while sending the metering data. Status code $status")
                            }
                        }

                        override fun onFailure(response: Response?, t: Throwable?, extendedInfo: JSONObject?) {
                            Logger.error("${extendedInfo.toString()}")
                        }

                    })
                    apiManager.execute()
                } catch (e: JSONException) {
                    Logger.error("${e.message}")
                }
            }

        }

    }

}