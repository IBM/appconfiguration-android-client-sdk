package com.ibm.cloud.appconfiguration.android.sdk.core

import com.ibm.cloud.appconfiguration.android.sdk.configurations.internal.URLBuilder
import com.ibm.cloud.sdk.core.http.RequestBuilder
import com.ibm.cloud.sdk.core.http.Response
import com.ibm.cloud.sdk.core.security.Authenticator
import com.ibm.cloud.sdk.core.security.NoAuthAuthenticator
import com.ibm.cloud.sdk.core.service.BaseService
import com.ibm.cloud.sdk.core.util.ResponseConverterUtils
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException


class ServiceImplTest : BaseService() {

    private var server = MockWebServer()

    @Before
    fun setUp() {
        try {
            server = MockWebServer()
            // register handler
            server.start(8080)
        } catch (err: IOException) {
            fail("Failed to instantiate mock web server")
        }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Throws(Throwable::class)
    fun constructClientService() {
        val serviceName = "testService"
        val authenticator: Authenticator = NoAuthAuthenticator()
        ServiceImpl(serviceName, authenticator)
    }

    /** Negative Test - construct the service with a null authenticator.  */
    @Test(expected = IllegalArgumentException::class)
    @Throws(Throwable::class)
    fun testWithNullAuthenticator() {
        val serviceName = "testService"
        ServiceImpl(serviceName, null)
    }

    /**
     * TODO
     */
    @Test
    @Throws(Throwable::class)
    fun testGetConfig() {
    }

    /**
     * TODO
     */
    @Test
    fun testPostMetering() {
    }
}