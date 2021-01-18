/*
 * (C) Copyright IBM Corp. 2021.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.ibm.appconfiguration.android.lib.feature.internal

import android.content.Context
import com.ibm.appconfiguration.android.lib.core.Logger
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader

internal object FileManager {

    private val fileLock = Any()
    const val fileName = "appconfiguration-features.json"

    fun storeFiles(context: Context, json: String): Boolean {
        synchronized(fileLock) {
            return try {
                val fileStorage = context.openFileOutput(
                    fileName,
                    Context.MODE_PRIVATE
                )
                if (Validators.validateString(
                        json
                    )
                ) {
                    fileStorage.write(json.toByteArray())
                }
                fileStorage.close()
                true
            } catch (e: FileNotFoundException) {
                Logger.error("Invalid action in FileManager class. ${e.message}")
                false
            } catch (e: IOException) {
                Logger.error("Invalid action in FileManager class. ${e.message}")
                false
            }
        }
    }

    fun getFileData(context: Context): JSONObject? {
        synchronized(fileLock) {
            return try {
                val fileStorage =
                    context.openFileInput(fileName)
                val isr = InputStreamReader(fileStorage)
                val bufferedReader = BufferedReader(isr)
                val sb = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                JSONObject(sb.toString())
            } catch (fileNotFound: FileNotFoundException) {
                Logger.error("Invalid action in FileManager class. ${fileNotFound.message}")
                null
            } catch (ioException: IOException) {
                Logger.error("Invalid action in FileManager class. ${ioException.message}")
                null
            } catch (e: JSONException) {
                Logger.error("Invalid action in FileManager class. ${e.message}")
                null
            }
        }
    }
}