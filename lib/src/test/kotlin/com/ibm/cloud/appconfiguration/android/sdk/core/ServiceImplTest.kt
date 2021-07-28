package com.ibm.cloud.appconfiguration.android.sdk.core

import com.ibm.cloud.sdk.core.service.BaseService
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
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

    /** Negative Test - construct the service with a null authenticator.  */
    @Test(expected = IllegalArgumentException::class)
    fun testWithNullAuthenticator() {
        val serviceName = "testService"
        ServiceImpl(serviceName, null)
    }

    @Test
    fun testGetConfig() {
        val test = ServiceImpl.getInstance()
        assertNull(test.getConfig("https://testConfig"))
    }

    @Test
    fun testPostMetering() {
        val test = ServiceImpl.getInstance()
        assertNull(test.postMetering("https://testMetering", JSONObject()))
    }
}