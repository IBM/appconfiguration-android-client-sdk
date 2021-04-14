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

package com.ibm.appconfiguration.android.lib.configurations.internal

import android.content.Context
import com.ibm.appconfiguration.android.lib.core.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap


private typealias meteringStore = ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>>>>

internal class Metering {

    private var sendInterval: Long = 600000
    private var appContext: Context? = null
    private var urlBuilder: URLBuilder? = null

    var meteringFeatureData: meteringStore = ConcurrentHashMap()
    var meteringPropertyData: meteringStore = ConcurrentHashMap()

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

    @Synchronized fun addMetering(guid: String, collectionId: String, identityId: String, segmentId: String, featureId: String?, propertyId: String?) {

        var hasData = false
        val featureJson: HashMap<String, Any> = HashMap()
        featureJson["count"] = 1
        val currentDate = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Instant.now().toString()
            } else {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
            }

        featureJson["evaluation_time"] = currentDate
        var meteringData: meteringStore =
            if (featureId != null) this.meteringFeatureData else this.meteringPropertyData
        val modifyKey: String = featureId ?: propertyId!!



        if (meteringData.containsKey(guid)) {
            if (meteringData[guid]!!.containsKey(collectionId)) {
                if (meteringData[guid]!![collectionId]!!.containsKey(modifyKey)) {
                    if (meteringData[guid]!![collectionId]!![modifyKey]!!.containsKey(identityId)) {
                        if (meteringData[guid]!![collectionId]!![modifyKey]!![identityId]!!.containsKey(
                                segmentId
                            )
                        ) {
                            hasData = true
                            meteringData[guid]!![collectionId]!![modifyKey]!![identityId]!![segmentId]!!["evaluation_time"] = currentDate
                            val count = meteringData[guid]!![collectionId]!![modifyKey]!![identityId]!![segmentId]!!["count"] as Int
                            meteringData[guid]!![collectionId]!![modifyKey]!![identityId]!![segmentId]!!["count"] = count + 1
                        }
                    } else {
                        var segmentIdMap = ConcurrentHashMap<String, HashMap<String, Any>>()
                        segmentIdMap[segmentId] = HashMap()
                        meteringData[guid]!![collectionId]!![modifyKey]!![identityId] = segmentIdMap
                    }
                } else {
                    var segmentIdMap = ConcurrentHashMap<String, HashMap<String, Any>>()
                    var identityIdMap =
                        ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>()
                    segmentIdMap[segmentId] = HashMap()
                    identityIdMap[identityId] = segmentIdMap
                    meteringData[guid]!![collectionId]!![modifyKey] = identityIdMap
                }
            } else {
                var segmentIdMap = ConcurrentHashMap<String, HashMap<String, Any>>()
                var identityIdMap =
                    ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>()
                var featureIdMap =
                    ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>>()
                segmentIdMap[segmentId] = HashMap()
                identityIdMap[identityId] = segmentIdMap
                featureIdMap[modifyKey] = identityIdMap
                meteringData[guid]!![collectionId] = featureIdMap
            }
        } else {
            var segmentIdMap = ConcurrentHashMap<String, HashMap<String, Any>>()
            var identityIdMap =
                ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>()
            var featureIdMap =
                ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>>()
            var collectionIdMap =
                ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>>>()
            segmentIdMap[segmentId] = HashMap()
            identityIdMap[identityId] = segmentIdMap
            featureIdMap[modifyKey] = identityIdMap
            collectionIdMap[collectionId] = featureIdMap
            meteringData[guid] = collectionIdMap
        }

        if (!hasData) {
            meteringData[guid]!![collectionId]!![modifyKey]!![identityId]!![segmentId] = featureJson
        }

    }

    @Synchronized private fun buildRequestBody(
        sendMeteringData: meteringStore,
        result: HashMap<String, JSONArray>,
        key: String
    ) {

        for(guidEntry in sendMeteringData) {

            val guid = guidEntry.key

            if (!result.containsKey(guid)) {
                result[guid] = JSONArray()
            }

            for ( collectionEntry in guidEntry.value) {
                val collections = JSONObject()
                collections.put("collection_id", collectionEntry.key);
                collections.put("usages", JSONArray())

                for (meterEntry in collectionEntry.value) {

                    for (identityEntry in meterEntry.value) {

                        for (segmentEntry in identityEntry.value) {

                            val usages = JSONObject()
                            usages.put(key, meterEntry.key)
                            usages.put("identity_id", identityEntry.key)
                            usages.put(
                                "segment_id",
                                if (segmentEntry.key === "$\$null$$") JSONObject.NULL else segmentEntry.key
                            )
                            usages.put("evaluation_time", segmentEntry.value["evaluation_time"])
                            usages.put("count", segmentEntry.value["count"])
                            collections.getJSONArray("usages").put(usages)
                        }
                    }
                }
                result[guid]?.put(collections);
            }
        }
    }
    fun sendMetering(): HashMap<String, JSONArray> {

//        if (appContext == null || urlBuilder == null) {
//            return HashMap()
//        }
        val sendFeatureData = this.meteringFeatureData
        val sendPropertyData = this.meteringPropertyData

        meteringFeatureData = ConcurrentHashMap()
        meteringPropertyData = ConcurrentHashMap()


        if (sendFeatureData.size <= 0 && sendPropertyData.size <= 0) {
            return HashMap()
        }

        var result: HashMap<String, JSONArray> = HashMap()

        if (sendFeatureData.size > 0) {
            this.buildRequestBody(sendFeatureData, result, "feature_id");
        }

        if (sendPropertyData.size > 0) {
            this.buildRequestBody(sendPropertyData, result, "property_id");
        }

        for (guidEntry in result) {
            for (i in 0 until guidEntry.value.length()) {
                val jsonObject = guidEntry.value.getJSONObject(i)
                this.sendToServer(guidEntry.key, jsonObject)
            }
        }
        return result
    }

    @Synchronized
    private fun sendToServer(guid: String, data: JSONObject) {

        val configURL = urlBuilder!!.getMeteringurl(guid) ?: ""
        val apiManager: APIManager =
            APIManager.newInstance(appContext!!, configURL, BaseRequest.POST)
        apiManager.setJSONRequestBody(data)
        apiManager.setResponseListener(object : ResponseListener {
            override fun onSuccess(response: Response) {

                val status = response.getStatus()
                if (status in ConfigConstants.REQUEST_SUCCESS_200..ConfigConstants.REQUEST_SUCCESS_299) {
                    Logger.debug("Successfully pushed the data to metering'")
                } else {
                    Logger.error("Error while sending the metering data. Status code is: $status. Response body: ${response.getResponseText()}")
                }
            }

            override fun onFailure(response: Response?, throwable: Throwable?, extendedInfo: JSONObject?) {
                Logger.error("${extendedInfo.toString()}")
            }
        })
        apiManager.execute()
    }

}
