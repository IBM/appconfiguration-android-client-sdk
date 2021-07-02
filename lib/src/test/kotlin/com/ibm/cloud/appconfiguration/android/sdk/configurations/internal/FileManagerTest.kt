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
import org.junit.Assert.*
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.eq
import java.io.*


class FileManagerTest {


    @Test
    fun testFileStoreError() {

        val applicationContext = Mockito.mock(Context::class.java)
        assertFalse(FileManager.storeFiles(applicationContext, "{\"message\": \"test\"}"))

    }

    @Test
    fun testFileStore() {

        val fos = Mockito.mock(FileOutputStream::class.java)
        val file = Mockito.mock(File::class.java)
        val applicationContext = Mockito.mock(Context::class.java)
        Mockito.`when`(applicationContext.getFileStreamPath(eq("appconfiguration.json"))).thenReturn(file)
        Mockito.`when`(applicationContext.openFileOutput(eq("appconfiguration.json"), ArgumentMatchers.anyInt())).thenReturn(fos)
        assertTrue(FileManager.storeFiles(applicationContext, "{\"message\": \"test\"}"))

    }

    @Test
    fun testFileStoreNotFound() {
        val applicationContext = Mockito.mock(Context::class.java)
        Mockito.`when`(applicationContext.openFileOutput(eq("appconfiguration.json"), ArgumentMatchers.anyInt())).thenThrow(FileNotFoundException("Error in finding the file"))
        assertFalse(FileManager.storeFiles(applicationContext, "{\"message\": \"test\"}"))
    }


    @Test
    fun testFileget() {
        val applicationContext = Mockito.mock(Context::class.java)
        assertNull(FileManager.getFileData(applicationContext))
    }

    @Test
    fun testFileGetContext() {
        val applicationContext = Mockito.mock(Context::class.java)
        val stream: FileInputStream = Mockito.mock(FileInputStream::class.java)
        `when`(applicationContext.openFileInput(anyString())).thenReturn(stream)
        assertNull(FileManager.getFileData(applicationContext))

    }

    @Test
    fun testFileGetContextError() {
        val applicationContext = Mockito.mock(Context::class.java)
        `when`(applicationContext.openFileInput(anyString())).thenThrow(FileNotFoundException("Error in finding the file"))
        assertNull(FileManager.getFileData(applicationContext))

    }
}