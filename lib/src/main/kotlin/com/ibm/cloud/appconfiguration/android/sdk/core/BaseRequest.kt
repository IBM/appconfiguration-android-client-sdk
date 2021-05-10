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

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.security.cert.CertificateException
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


open class BaseRequest constructor(
    url: String,
    method: String,
    timeout: Int = 5000,
    autoRetries: Int = 0
) {

    // Headers
    val CONTENT_TYPE = "Content-Type"
    val TEXT_PLAIN_CONTENT_TYPE = "text/plain"

    protected var numberOfRetries = 0
    private var url: String = ""
    private var method: String
    private var timeout: Int = 0
    private var headers = Headers.Builder()
    private val httpClient = getUnsafeOkHttpClient()

    companion object {

        /**
         * The string constant for the GET HTTP method verb.
         */
        val GET = "GET"

        /**
         * The string constant for the POST HTTP method verb.
         */
        val POST = "POST"

        fun getUnsafeOkHttpClient(): OkHttpClient.Builder {

            try {
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<java.security.cert.X509Certificate>,
                        authType: String
                    ) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<java.security.cert.X509Certificate>,
                        authType: String
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                        return arrayOf()
                    }
                })

                // Install the all-trusting trust manager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                // Create an ssl socket factory with our all-trusting manager
                val sslSocketFactory = sslContext.socketFactory

                val builder = OkHttpClient.Builder()
                builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                builder.hostnameVerifier(HostnameVerifier { _, _ -> true })
                return builder
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

    /**
     * Constructs a new request with the specified URL, using the specified HTTP method.
     * Additionally this constructor sets a custom timeout.
     *
     * @param url     The resource URL
     * @param method  The HTTP method to use.
     * @param timeout The timeout in milliseconds for this request.
     * @param autoRetries  The number of times to retry each request if it fails due to timeout or loss of network connection.
     */
    init {
        this.url = url
        this.method = method
        this.numberOfRetries = autoRetries
        setTimeout(timeout)
    }

    private fun setTimeout(timeout: Int) {
        this.timeout = timeout

        this.httpClient.connectTimeout(
            timeout.toLong(),
            TimeUnit.MILLISECONDS
        )
        this.httpClient.readTimeout(
            timeout.toLong(),
            TimeUnit.MILLISECONDS
        )
        this.httpClient.writeTimeout(
            timeout.toLong(),
            TimeUnit.MILLISECONDS
        )
    }

    /**
     * Adds a header to this resource request. This method allows request headers to have multiple values.
     *
     * @param name  The name of the header to add
     * @param value The value of the header to add
     */
    open fun addHeader(name: String, value: String) {
        headers.add(name, value)
    }

    /**
     * Returns the URL for this resource request.
     *
     * @return String The URL representing the path for this resource request.
     */
    open fun getUrl(): String? {
        return url
    }

    /**
     * Returns the HTTP method for this resource request.
     *
     * @return A string containing the name of the HTTP method.
     */
    open fun getMethod(): String? {
        return method
    }

    /**
     * Returns all the headers that were set for this resource request.
     *
     * @return An array of Headers
     */
    open fun getAllHeaders(): Map<String, List<String>>? {
        return headers.build().toMultimap()
    }

    /**
     * Returns the timeout for this resource request.
     *
     * @return the timeout for this resource request
     */
    open fun getTimeout(): Int {
        return timeout
    }


    /**
     * Send this resource request asynchronously, without a request body.
     *
     * @param listener  The listener whose onSuccess or onFailure methods will be called when this request finishes
     */
    open fun send(listener: ResponseListener) {
        send("", listener)
    }

    /**
     * Send this resource request asynchronously, with the given string as the request body.
     * If the Content-Type header was not previously set, this method will set it to "text/plain".
     *
     * @param requestBody   The text to put in the request body
     * @param listener      The listener whose onSuccess or onFailure methods will be called when this request finishes
     */
    open fun send(requestBody: String?, listener: ResponseListener) {
        var contentType = headers[CONTENT_TYPE]
        if (contentType == null) {
            contentType = TEXT_PLAIN_CONTENT_TYPE
        }

        // If the request body is an empty string, it should be treated as null
        var body: RequestBody? = null
        if (requestBody != null && requestBody.isNotEmpty()) {
            body = requestBody.toRequestBody(contentType.toMediaTypeOrNull())
        }
        sendRequest(listener, body)
    }

    private fun sendRequest(responseListener: ResponseListener, requestBody: RequestBody?) {
        if (!isValidMethod(method)) {
            responseListener.onFailure(
                null,
                IllegalArgumentException("Method is not valid: $method"),
                null
            )
            return
        }

        val requestBuilder = Request.Builder()
        requestBuilder.headers(headers.build())

        try {
            requestBuilder.url(url)
        } catch (e: Exception) {
            responseListener.onFailure(null, e, null)
            return
        }

        if (method.equals(GET, ignoreCase = true)) requestBuilder.get() else requestBuilder.method(
            method,
            requestBody
        )

        val request = requestBuilder.build()
        sendOKHttpRequest(request, getCallback(responseListener))

    }

    protected open fun sendOKHttpRequest(request: Request, callback: Callback) {
        val client: OkHttpClient = httpClient.build()
        client.newCall(request).enqueue(callback)
    }

    private fun getCallback(responseListener: ResponseListener): Callback {
        return object : Callback {
            override fun onFailure(call: Call, e: IOException) {

                // If auto-retries are enabled, and the request hasn't run out of retry attempts,
                // then try to send the same request again. Otherwise, delegate to the user's ResponseListener.
                // Note that we also retry requests that receive 504 responses, as seen in the onResponse() method.
                if (numberOfRetries > 0) {
                    numberOfRetries--
                    Logger.debug(
                        "Resending ${call.request().method} request to ${call.request()
                            .toString()}")
                    sendOKHttpRequest(
                        call.request(),
                        getCallback(responseListener)
                    )
                } else {
                    responseListener.onFailure(null, e, null)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {
                if (response.isSuccessful || response.isRedirect) {
                    val bmsResponse: Response = ResponseImpl(response)
                    responseListener.onSuccess(bmsResponse)
                } else {
                    responseListener.onFailure(ResponseImpl(response), null, null)
                }
                response.body!!.close()
            }
        }
    }

    private fun isValidMethod(method: String): Boolean {
        return method.equals(POST, ignoreCase = true) ||
                method.equals(GET, ignoreCase = true)
    }
}