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

import android.content.Context
import com.ibm.cloud.appconfiguration.android.sdk.AppConfiguration
import org.json.JSONObject

open class APIManager(url: String, method: String) : ResponseListener {

    private val X_REWRITE_DOMAIN = "X-REWRITE-DOMAIN"
    private val AUTHORIZATION = "Authorization"
    private val APPLICATION_JSON = "application/json"
    private val CONTENT_TYPE = "Content-Type"


    private var request: BaseRequest? = null
    private var responseListener: ResponseListener? = null
    private var requestBody: JSONObject? = null

    init {
        request = BaseRequest(url, method, 10000)
        request!!.addHeader(
            AUTHORIZATION,
            AppConfiguration.getInstance().getApikey()
        )
        request!!.addHeader(CONTENT_TYPE, APPLICATION_JSON)
        if (AppConfiguration.overrideServerHost != null) {
            request!!.addHeader(
                X_REWRITE_DOMAIN,
                AppConfiguration.overrideServerHost!!
            )
        }
    }

    companion object {
        private var appContext: Context? = null
        fun newInstance(
            ctx: Context,
            url: String,
            method: String
        ): APIManager {
            this.appContext = ctx
            return APIManager(url, method)
        }
    }

    open fun setResponseListener(listener: ResponseListener?) {
        responseListener = listener
    }

    open fun setJSONRequestBody(json: JSONObject) {
        requestBody = json
    }

    open fun execute() {
        Logger.debug(
            " Sending request to AppConfiguration server, with url = ${(request?.getUrl() ?: "")
                .toString()} with http method = ${(request?.getMethod() ?: "")}")
        if (requestBody != null && requestBody!!.length() != 0) {
            request?.send(requestBody.toString(), this)
        } else {
            request?.send(this)
        }
    }

    override fun onSuccess(response: Response) {
        Logger.debug("Success response in invoker is: $response")
        responseListener?.onSuccess(response)
    }

    override fun onFailure(response: Response?, throwable: Throwable?, extendedInfo: JSONObject?) {
        responseListener?.onFailure(response, throwable,extendedInfo )
    }
}