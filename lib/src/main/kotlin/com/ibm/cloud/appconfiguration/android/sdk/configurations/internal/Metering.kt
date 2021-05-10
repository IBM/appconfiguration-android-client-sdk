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
import com.ibm.cloud.appconfiguration.android.sdk.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap


private typealias meteringStore = ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>>>>>

internal class Metering {

    private var sendInterval: Long = 600000
    private var appContext: Context? = null
    private var urlBuilder: URLBuilder? = null

    var meteringFeatureData: meteringStore = ConcurrentHashMap()
    var meteringPropertyData: meteringStore = ConcurrentHashMap()

    val mutex = Mutex()

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

        GlobalScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(sendInterval)
                sendMetering()
            }
        }
    }

    @Synchronized fun addMetering(
        guid: String,
        environmentId: String,
        collectionId: String,
        identityId: String,
        segmentId: String,
        featureId: String?,
        propertyId: String?
    ) {

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

            if (meteringData[guid]!!.containsKey(environmentId)) {

                if (meteringData[guid]!![environmentId]!!.containsKey(collectionId)) {
                    if (meteringData[guid]!![environmentId]!![collectionId]!!.containsKey(modifyKey)) {
                        if (meteringData[guid]!![environmentId]!![collectionId]!![modifyKey]!!.containsKey(
                                identityId
                            )) {
                            if (meteringData[guid]!![environmentId]!![collectionId]!![modifyKey]!![identityId]!!.containsKey(
                                    segmentId
                                )
                            ) {
                                hasData = true
                                meteringData[guid]!![environmentId]!![collectionId]!![modifyKey]!![identityId]!![segmentId]!!["evaluation_time"] = currentDate
                                val count = meteringData[guid]!![environmentId]!![collectionId]!![modifyKey]!![identityId]!![segmentId]!!["count"] as Int
                                meteringData[guid]!![environmentId]!![collectionId]!![modifyKey]!![identityId]!![segmentId]!!["count"] = count + 1
                            }
                        } else {
                            var segmentIdMap = ConcurrentHashMap<String, HashMap<String, Any>>()
                            segmentIdMap[segmentId] = HashMap()
                            meteringData[guid]!![environmentId]!![collectionId]!![modifyKey]!![identityId] = segmentIdMap
                        }
                    } else {
                        var segmentIdMap = ConcurrentHashMap<String, HashMap<String, Any>>()
                        var identityIdMap =
                            ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>()
                        segmentIdMap[segmentId] = HashMap()
                        identityIdMap[identityId] = segmentIdMap
                        meteringData[guid]!![environmentId]!![collectionId]!![modifyKey] = identityIdMap
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
                    meteringData[guid]!![environmentId]!![collectionId] = featureIdMap
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
                meteringData[guid]!![environmentId] = collectionIdMap

            }
        } else {
            var segmentIdMap = ConcurrentHashMap<String, HashMap<String, Any>>()
            var identityIdMap =
                ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>()
            var featureIdMap =
                ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>>()
            var collectionIdMap =
                ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>>>()
            var environmentIdMap =
                ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>>>>()
            segmentIdMap[segmentId] = HashMap()
            identityIdMap[identityId] = segmentIdMap
            featureIdMap[modifyKey] = identityIdMap
            collectionIdMap[collectionId] = featureIdMap
            environmentIdMap[environmentId] = collectionIdMap
            meteringData[guid] = environmentIdMap
        }

        if (!hasData) {
            meteringData[guid]!![environmentId]!![collectionId]!![modifyKey]!![identityId]!![segmentId] = featureJson
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

            for (environmentEntry in guidEntry.value) {

                for ( collectionEntry in environmentEntry.value) {
                    val collections = JSONObject()
                    collections.put("collection_id", collectionEntry.key)
                    collections.put("environment_id", environmentEntry.key)
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
                    result[guid]?.put(collections)
                }
            }

        }
    }
    fun sendMetering(): HashMap<String, JSONArray> {

        val sendFeatureData = this.meteringFeatureData
        val sendPropertyData = this.meteringPropertyData

        meteringFeatureData = ConcurrentHashMap()
        meteringPropertyData = ConcurrentHashMap()


        if (sendFeatureData.size <= 0 && sendPropertyData.size <= 0) {
            return HashMap()
        }

        var result: HashMap<String, JSONArray> = HashMap()

        if (sendFeatureData.size > 0) {
            this.buildRequestBody(sendFeatureData, result, "feature_id")
        }

        if (sendPropertyData.size > 0) {
            this.buildRequestBody(sendPropertyData, result, "property_id")
        }

        for (guidEntry in result) {
            for (i in 0 until guidEntry.value.length()) {
                val jsonObject = guidEntry.value.getJSONObject(i)
                val count = jsonObject.getJSONArray("usages").length()
                if (count > 25) {
                    GlobalScope.launch(Dispatchers.IO) {
                        sendSplitMetering(guidEntry.key, jsonObject, count)
                    }
                } else {
                    this.sendToServer(guidEntry.key, jsonObject)
                }
            }
        }
        return result
    }

    private suspend fun sendSplitMetering(guid: String?, data: JSONObject, count: Int) {
        var lim = 0
        val subUsagesArray = data.getJSONArray("usages")
        while (lim <= count) {
            val endIndex =
                if (lim + ConfigConstants.DEFAULT_USAGE_LIMIT >= count) count else lim + ConfigConstants.DEFAULT_USAGE_LIMIT
            val collectionsMap = JSONObject()
            collectionsMap.put("collection_id", data.getString("collection_id"))
            collectionsMap.put("environment_id", data.getString("environment_id"))
            val usagesArray = JSONArray()
            for (i in lim until endIndex) {
                usagesArray.put(subUsagesArray[i])
            }
            collectionsMap.put("usages", usagesArray)
            mutex.withLock {
                sendToServer(guid!!, collectionsMap)
            }

            lim += ConfigConstants.DEFAULT_USAGE_LIMIT
        }
    }

    private fun sendToServer(guid: String, data: JSONObject) {

        if (appContext == null || urlBuilder == null) {
            return
        }
        val configURL = urlBuilder!!.getMeteringUrl(guid) ?: ""
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

            override fun onFailure(
                response: Response?,
                throwable: Throwable?,
                extendedInfo: JSONObject?
            ) {
                Logger.error("${extendedInfo.toString()}")
            }
        })
        apiManager.execute()
    }

}
