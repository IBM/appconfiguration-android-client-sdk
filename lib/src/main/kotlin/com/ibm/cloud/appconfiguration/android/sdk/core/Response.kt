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

import org.json.JSONObject
import java.io.InputStream

interface Response {

    /**
     * Returns the URL that the request was made to.
     *
     * @return The URL of the request.
     */
    fun getRequestURL(): String?

    /**
     * This method gets the HTTP status of the response.
     *
     * @return The HTTP status of the response. Will be 0 when there was no response.
     */
    fun getStatus(): Int

    /**
     * This method parses the response body as a String.
     * If this method is called, then subsequent calls to [.getResponseByteStream] or [.getResponseBytes]
     * will return null unless the [Request] was made using a `download()` method.
     *
     * @return The body of the response as a String. Empty string if there is no body.
     */
    fun getResponseText(): String?

    /**
     * This method parses the response body as a JSONObject.
     * If this method is called, then subsequent calls to [.getResponseByteStream] or [.getResponseBytes]
     * will return null unless the [Request] was made using a `download()` method.
     *
     * @return The body of the response as a JSONObject.
     */
    fun getResponseJSON(): JSONObject?

    /**
     * This method gets the bytes of the response body.
     * If this method is called, then subsequent calls to [.getResponseByteStream] or [.getResponseBytes]
     * will return null unless the [Request] was made using a `download()` method.
     *
     * @return the bytes of the response body. Will be null if there is no body.
     */
    fun getResponseBytes(): ByteArray?

    /**
     * This method gets the response body as an input stream.
     *
     *
     *
     * **Important: **This method may not be used for requests made with any of the [Request] download() methods,
     * since the stream will already be closed. Use [Response.getResponseBytes] instead.
     *
     *
     * @return The input stream representing the response body. Will be null if there is no body.
     */
    fun getResponseByteStream(): InputStream?

    /**
     * This method gets the Content-Length of the response body.
     *
     * @return The content length of the response.
     */
    fun getContentLength(): Long

    /**
     * Get the HTTP headers from the response.
     *
     * @return A map with all the headers, and the corresponding values for each one.
     */
    fun getHeaders(): Map<String, List<String>>?
}