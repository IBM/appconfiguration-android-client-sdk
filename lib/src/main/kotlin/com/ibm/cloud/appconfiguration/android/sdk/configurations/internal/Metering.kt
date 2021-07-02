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
import com.ibm.cloud.appconfiguration.android.sdk.core.Logger
import com.ibm.cloud.appconfiguration.android.sdk.core.ServiceImpl
import com.ibm.cloud.sdk.core.http.Response
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

/**
 * Class consisting of functions that stores the feature and property evaluations metrics and send the metrics
 * to App Configuration server in intervals.
 */
internal class Metering {

    private var sendInterval: Long = 600000
    private var appContext: Context? = null
    private var urlBuilder: URLBuilder? = null

    private var meteringFeatureData: meteringStore = ConcurrentHashMap()
    private var meteringPropertyData: meteringStore = ConcurrentHashMap()

    private val mutex = Mutex()

    companion object Factory {
        private var instance: Metering? = null

        /**
         * @return instance of [Metering]
         */
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

    /**
     * Stores the feature and property evaluation metrics into hashmaps.
     *
     * @param guid guid of App Configuration service instance
     * @param environmentId environment id of App Configuration service instance
     * @param collectionId collection id
     * @param entityId entity id
     * @param segmentId segment id
     * @param featureId feature id
     * @param propertyId property id
     */
    @Synchronized
    fun addMetering(
        guid: String,
        environmentId: String,
        collectionId: String,
        entityId: String,
        segmentId: String,
        featureId: String?,
        propertyId: String?
    ) {

        var hasData = false
        val featureJson: HashMap<String, Any> = HashMap()
        featureJson["count"] = 1
        val currentDate = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Instant.now().toString().split(".").toTypedArray()[0] + "Z"
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
                        if (meteringData[guid]!![environmentId]!![collectionId]!![modifyKey]!!.containsKey(entityId)) {
                            if (meteringData[guid]!![environmentId]!![collectionId]!![modifyKey]!![entityId]!!.containsKey(segmentId)) {
                                hasData = true
                                meteringData[guid]!![environmentId]!![collectionId]!![modifyKey]!![entityId]!![segmentId]!!["evaluation_time"] = currentDate
                                val count = meteringData[guid]!![environmentId]!![collectionId]!![modifyKey]!![entityId]!![segmentId]!!["count"] as Int
                                meteringData[guid]!![environmentId]!![collectionId]!![modifyKey]!![entityId]!![segmentId]!!["count"] = count + 1
                            }
                        } else {
                            var segmentIdMap = ConcurrentHashMap<String, HashMap<String, Any>>()
                            segmentIdMap[segmentId] = HashMap()
                            meteringData[guid]!![environmentId]!![collectionId]!![modifyKey]!![entityId] = segmentIdMap
                        }
                    } else {
                        var segmentIdMap = ConcurrentHashMap<String, HashMap<String, Any>>()
                        var entityIdMap =
                            ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>()
                        segmentIdMap[segmentId] = HashMap()
                        entityIdMap[entityId] = segmentIdMap
                        meteringData[guid]!![environmentId]!![collectionId]!![modifyKey] = entityIdMap
                    }
                } else {
                    var segmentIdMap = ConcurrentHashMap<String, HashMap<String, Any>>()
                    var entityIdMap =
                        ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>()
                    var featureIdMap =
                        ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>>()
                    segmentIdMap[segmentId] = HashMap()
                    entityIdMap[entityId] = segmentIdMap
                    featureIdMap[modifyKey] = entityIdMap
                    meteringData[guid]!![environmentId]!![collectionId] = featureIdMap
                }

            } else {

                var segmentIdMap = ConcurrentHashMap<String, HashMap<String, Any>>()
                var entityIdMap =
                    ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>()
                var featureIdMap =
                    ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>>()
                var collectionIdMap =
                    ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>>>()
                segmentIdMap[segmentId] = HashMap()
                entityIdMap[entityId] = segmentIdMap
                featureIdMap[modifyKey] = entityIdMap
                collectionIdMap[collectionId] = featureIdMap
                meteringData[guid]!![environmentId] = collectionIdMap

            }
        } else {
            var segmentIdMap = ConcurrentHashMap<String, HashMap<String, Any>>()
            var entityIdMap =
                ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>()
            var featureIdMap =
                ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>>()
            var collectionIdMap =
                ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>>>()
            var environmentIdMap =
                ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, HashMap<String, Any>>>>>>()
            segmentIdMap[segmentId] = HashMap()
            entityIdMap[entityId] = segmentIdMap
            featureIdMap[modifyKey] = entityIdMap
            collectionIdMap[collectionId] = featureIdMap
            environmentIdMap[environmentId] = collectionIdMap
            meteringData[guid] = environmentIdMap
        }

        if (!hasData) {
            meteringData[guid]!![environmentId]!![collectionId]!![modifyKey]!![entityId]!![segmentId] = featureJson
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

                        for (entityEntry in meterEntry.value) {

                            for (segmentEntry in entityEntry.value) {

                                val usages = JSONObject()
                                usages.put(key, meterEntry.key)
                                usages.put("entity_id", entityEntry.key)
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

    /**
     * Sends the evaluation metrics data to App Configuration billing server.
     *
     * @return JSON data constructed out of hashmaps
     */
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

    suspend fun sendSplitMetering(guid: String?, data: JSONObject, count: Int): JSONArray {
        var lim = 0
        val subUsagesArray = data.getJSONArray("usages")
        var result = JSONArray()
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
            result.put(collectionsMap)
            mutex.withLock {
                sendToServer(guid!!, collectionsMap)
            }

            lim += ConfigConstants.DEFAULT_USAGE_LIMIT
        }
        return result
    }

    private fun sendToServer(guid: String, data: JSONObject) {

        if (appContext == null || urlBuilder == null) {
            return
        }
        val meteringURL = urlBuilder!!.getMeteringUrl(guid)
        val response: Response<String>? = ServiceImpl.getInstance().postMetering(meteringURL, data)
        if (response != null) {
            if (response.statusCode >= ConfigConstants.REQUEST_SUCCESS_200
                && response.statusCode <= ConfigConstants.REQUEST_SUCCESS_299
            ) {
                Logger.debug("Successfully pushed the data to metering")
            } else {
                Logger.error(
                    "Error while sending the metering data. Status code is: ${response.statusCode}." +
                            "Response body: ${response.statusMessage}"
                )
            }
        }
    }

}
