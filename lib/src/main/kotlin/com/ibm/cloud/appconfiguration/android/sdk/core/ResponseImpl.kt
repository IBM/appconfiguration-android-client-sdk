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

import okhttp3.Headers
import okhttp3.MediaType
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException

internal class ResponseImpl constructor(response: okhttp3.Response) : Response {

    private var okHttpResponse: okhttp3.Response? = null
    private var requestURL: String? = null
    private var headers: Headers? = null
    private var contentType: MediaType? = null
    private var responseByteStream: InputStream? = null
    private var bodyBytes: ByteArray? = null

    init {
        this.okHttpResponse = response
        if (okHttpResponse != null) {
            requestURL = okHttpResponse!!.request.toString()
            headers = okHttpResponse!!.headers
            try {
                contentType = okHttpResponse!!.body!!.contentType()
                responseByteStream = okHttpResponse!!.body!!.byteStream()
                bodyBytes = okHttpResponse!!.body!!.bytes()
            } catch (e: NullPointerException) {
                Logger.error("Response body bytes can't be read: ${e.localizedMessage}")
                bodyBytes = null
            } catch (e: IOException) {
                Logger.error("Response body bytes can't be read: ${e.localizedMessage}")
                bodyBytes = null
            }
        }
    }


    override fun getRequestURL(): String? {
        return okHttpResponse?.request.toString()
    }

    override fun getStatus(): Int {
        return if (okHttpResponse == null) 0 else okHttpResponse!!.code
    }

    override fun getResponseText(): String? {
        if (bodyBytes == null) {
            return ""
        }
        val charset =
            if (contentType != null) contentType!!.charset(Charsets.UTF_8) else Charsets.UTF_8
        return try {
            (if (charset != null) {
                String(bodyBytes!!, charset)
            } else "").toString()
        } catch (e: UnsupportedEncodingException) {
            Logger.warning("Failed to extract text from response body. Error: ${e.message}")
            null
        }
    }

    override fun getResponseJSON(): JSONObject? {
        val responseText = getResponseText()

        if (responseText == null || responseText.isEmpty()) {
            return null
        }
        return try {
            JSONObject(responseText)
        } catch (e: JSONException) {
            Logger.warning("Failed to extract JSON from response body. Error: ${e.message}")
            null
        }
    }

    override fun getResponseBytes(): ByteArray? {
        return if (responseByteStream != null) {
            try {
                responseByteStream!!.readBytes()
            } catch (e: IOException) {
                Logger.warning("Failed to extract byte array from response body. Error: ${e.message}")
                null
            }
        } else bodyBytes
    }

    protected fun setResponseBytes(responseBytes: ByteArray?) {
        bodyBytes = responseBytes
    }

    override fun getResponseByteStream(): InputStream? {
        return responseByteStream
    }

    fun isRedirect(): Boolean {
        return if (okHttpResponse == null) {
            false
        } else okHttpResponse!!.isRedirect
    }

    fun isSuccessful(): Boolean {
        return if (okHttpResponse == null) {
            false
        } else okHttpResponse!!.isSuccessful
    }

    override fun getContentLength(): Long {
        return try {
            val resp = getInternalResponse()
            if (resp != null) {
                resp.body!!.contentLength()
            } else {
                0
            }
        } catch (e: java.lang.NullPointerException) {
            Logger.error("Failed to get the response content length from ${getRequestURL()}. Error: ${e.message}")
            0
        }
    }

    override fun getHeaders(): Map<String, List<String>>? {
        return if (headers == null) {
            null
        } else headers!!.toMultimap()
    }

    override fun toString(): String {
        return try {
            "Response: Status=${getStatus()}, Response Text: ${getResponseText()}"
        } catch (e: RuntimeException) {
            "Response: Status= ${getStatus()}, Exception occurred when constructing response text string: ${e.localizedMessage}"
        }
    }

    private fun getInternalResponse(): okhttp3.Response? {
        return okHttpResponse
    }

}