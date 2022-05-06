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

package com.ibm.cloud.appconfiguration.android.sdk.core

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.ibm.cloud.appconfiguration.android.sdk.AppConfiguration
import com.ibm.cloud.appconfiguration.android.sdk.BuildConfig
import com.ibm.cloud.appconfiguration.android.sdk.configurations.internal.ConfigConstants
import com.ibm.cloud.sdk.core.http.HttpHeaders
import com.ibm.cloud.sdk.core.http.HttpMediaType
import com.ibm.cloud.sdk.core.http.RequestBuilder
import com.ibm.cloud.sdk.core.http.Response
import com.ibm.cloud.sdk.core.security.Authenticator
import com.ibm.cloud.sdk.core.security.IamAuthenticator
import com.ibm.cloud.sdk.core.service.BaseService
import com.ibm.cloud.sdk.core.util.ResponseConverterUtils
import org.json.JSONObject

/**
 * A wrapper class consisting of methods that perform API request/response handling of the AppConfiguration SDK
 * by extending the [BaseService].
 */
class ServiceImpl(name: String?, authenticator: Authenticator?) : BaseService(name, authenticator) {

    companion object Factory {
        private var instance: ServiceImpl? = null

        /**
         * @return instance of [ServiceImpl]
         */
        fun getInstance(): ServiceImpl {
            if (instance == null)
                instance =
                    ServiceImpl(ConfigConstants.SERVICE_NAME, createIamAuth())
            return instance!!
        }

        private fun createIamAuth(): IamAuthenticator? {
            return if (AppConfiguration().getOverrideServiceUrl() != null) {
                IamAuthenticator.Builder()
                    .url(ConfigConstants.DEFAULT_HTTP_TYPE + ConfigConstants.DEFAULT_IAM_DEV_STAGE_URL)
                    .apikey(AppConfiguration.getInstance().getApikey())
                    .build()
            } else {
                //this automatically calls iam prod url
                IamAuthenticator.Builder()
                    .apikey(AppConfiguration.getInstance().getApikey())
                    .build()
            }
        }
    }

    private fun getServiceHeaders(): HashMap<String, String> {
        val headers: HashMap<String, String> = HashMap()
        headers[HttpHeaders.ACCEPT] = HttpMediaType.APPLICATION_JSON
        headers[HttpHeaders.USER_AGENT] =
            BuildConfig.LIBRARY_PACKAGE_NAME + "/" + BuildConfig.VERSION_NAME
        return headers
    }

    /**
     * Execute GET API request.
     *
     * @param url url to get configurations
     * @return the HTTP response
     */
    fun getConfig(url: String?): Response<String>? {

        return try {
            Logger.debug("Sending request to AppConfiguration server with url = $url")
            val builder = RequestBuilder.get(RequestBuilder.resolveRequestUrl(url, null, null))
            for ((key, value) in getServiceHeaders()) {
                builder.header(key, value)
            }
            val responseConverter = ResponseConverterUtils.getString()
            createServiceCall(builder.build(), responseConverter).execute()
        } catch (e: Exception) {
            e.localizedMessage?.let { Logger.error(it) }
            null
        }
    }

    /**
     * Execute POST API request.
     *
     * @param meteringUrl url to send metering data
     * @param data data to send
     * @return the HTTP response
     */
    fun postMetering(meteringUrl: String?, data: JSONObject): Response<String>? {

        return try {
            val builder =
                RequestBuilder.post(RequestBuilder.resolveRequestUrl(meteringUrl, null, null))
            for ((key, value) in getServiceHeaders()) {
                builder.header(key, value)
            }
            builder.header(HttpHeaders.CONTENT_TYPE, "application/json")
            val jsonData = Gson().fromJson(data.toString(), JsonObject::class.java)
            builder.bodyJson(jsonData)
            val responseConverter = ResponseConverterUtils.getString()
            createServiceCall(builder.build(), responseConverter).execute()
        } catch (e: Exception) {
            e.localizedMessage?.let { Logger.error(it) }
            null
        }
    }
}