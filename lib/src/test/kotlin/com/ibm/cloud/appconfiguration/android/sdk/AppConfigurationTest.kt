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
package com.ibm.cloud.appconfiguration.android.sdk

import android.content.Context
import com.ibm.cloud.appconfiguration.android.sdk.configurations.ConfigurationUpdateListener
import com.ibm.cloud.appconfiguration.android.sdk.configurations.internal.FileManager
import com.ibm.cloud.appconfiguration.android.sdk.configurations.models.Property
import io.mockk.every
import io.mockk.mockkObject
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.kotlin.eq
import java.io.*
import java.util.concurrent.TimeUnit


class AppConfigurationTest {

    private var server = MockWebServer()


    @Before
    fun setup() {
        server.start(8080)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    public fun testConfigurationHandler() {

        val appConfiguration = AppConfiguration.getInstance()
        AppConfiguration.overrideServiceUrl("https://localhost:8080")
        assertTrue(AppConfiguration().getOverrideServiceUrl() == "https://localh" +
                "ost:8080");

        appConfiguration.setContext("collectionId", "environmentId");

        val applicationContext = Mockito.mock(Context::class.java)

        appConfiguration.init(applicationContext, "", "guid", "apikey");
        appConfiguration.init(applicationContext, "region", "", "apikey");

        appConfiguration.init(applicationContext, "region", "guid", "");
        appConfiguration.init(applicationContext, "region", "guid", "apikey");

        appConfiguration.setContext("", "");

        appConfiguration.fetchConfigurations()
        appConfiguration.enableDebug(true);

        assertNull(appConfiguration.getFeatures());
        assertNull(appConfiguration.getProperties());


        val responseData = FileUtil.readFileWithoutNewLineFromResources("sample.json")
        var jsonResponseData = JSONObject(responseData)

        val response = MockResponse().setResponseCode(200)
            .setBody(responseData)

        server.enqueue(response)

        appConfiguration.init(applicationContext, "region", "guid", "apikey");
        appConfiguration.setContext("collectionId", "environmentId");


        val onData = arrayOf(false)

        appConfiguration.registerConfigurationUpdateListener(object : ConfigurationUpdateListener {
            override fun onConfigurationUpdate() {
                onData[0] = true
            }
        })


        val fos = mock(FileOutputStream::class.java)
        val file = mock(File::class.java)

        `when`(applicationContext.getFileStreamPath(eq("appconfiguration.json"))).thenReturn(file)
        `when`(applicationContext.openFileOutput(eq("appconfiguration.json"), anyInt())).thenReturn(fos)

        mockkObject(FileManager)
        every { FileManager.getFileData(applicationContext) } returns jsonResponseData
        every { FileManager.storeFiles(applicationContext, "") } returns true

        TimeUnit.SECONDS.sleep(2);
        assertFalse(onData[0]);

        assertTrue(appConfiguration.getFeatures()!!.size == 3)
        val feature = appConfiguration.getFeature("defaultfeature")
        val idVal = feature!!.getFeatureId()

        assertTrue(idVal == "defaultfeature")
        assertTrue(appConfiguration.getProperties()!!.size == 1)
        val property: Property? = appConfiguration.getProperty("numericproperty")
        assertTrue(property!!.getPropertyId().equals("numericproperty"))

        val attributes = JSONObject()
        attributes.put("email", "dev@tester.com")

        assertEquals("Welcome", feature.getCurrentValue("pqvr", attributes))
        assertEquals(81, property.getCurrentValue("pqvr", attributes))

    }

    object FileUtil {
        @Throws(IOException::class)
        fun readFileWithoutNewLineFromResources(fileName: String): String {
            var inputStream: InputStream? = null
            try {
                inputStream = getInputStreamFromResource(fileName)
                val builder = StringBuilder()
                val reader = BufferedReader(InputStreamReader(inputStream))

                var str: String? = reader.readLine()
                while (str != null) {
                    builder.append(str)
                    str = reader.readLine()
                }
                return builder.toString()
            } finally {
                inputStream?.close()
            }
        }

        @Throws(IOException::class)
        fun readFileWithNewLineFromResources(fileName: String): String {
            var inputStream: InputStream? = null
            try {
                inputStream = getInputStreamFromResource(fileName)
                val builder = StringBuilder()
                val reader = BufferedReader(InputStreamReader(inputStream))

                var theCharNum = reader.read()
                while (theCharNum != -1) {
                    builder.append(theCharNum.toChar())
                    theCharNum = reader.read()
                }

                return builder.toString()
            } finally {
                inputStream?.close()
            }
        }

        fun kotlinReadFileWithNewLineFromResources(fileName: String): String {
            return getInputStreamFromResource(fileName)?.bufferedReader()
                .use { bufferReader -> bufferReader?.readText() } ?: ""
        }

        @Throws(IOException::class)
        fun readBinaryFileFromResources(fileName: String): ByteArray {
            var inputStream: InputStream? = null
            val byteStream = ByteArrayOutputStream()
            try {
                inputStream = getInputStreamFromResource(fileName)

                var nextValue = inputStream?.read() ?: -1

                while (nextValue != -1) {
                    byteStream.write(nextValue)
                    nextValue = inputStream?.read() ?: -1
                }
                return byteStream.toByteArray()

            } finally {
                inputStream?.close()
                byteStream.close()
            }
        }

        fun kotlinReadBinaryFileFromResources(fileName: String): ByteArray {
            ByteArrayOutputStream().use { byteStream ->
                getInputStreamFromResource(fileName)?.copyTo(byteStream)
                return byteStream.toByteArray()
            }
        }

        private fun getInputStreamFromResource(fileName: String)
                = javaClass.classLoader?.getResourceAsStream(fileName)
    }

}