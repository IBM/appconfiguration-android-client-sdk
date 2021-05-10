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

import android.util.Log
import java.util.*

open class Logger {

    companion object {
        protected var is_debug = false

        enum class LEVEL(val value: String) {
            SUCCESS("SUCCESS"),
            ERROR("ERROR"),
            WARN("WARNING"),
            INFO("INFO"),
            DEBUG("DEBUG")
        }

        fun setDebug(value: Boolean) {
            this.is_debug = value
        }

        fun isDebug(): Boolean {
            return this.is_debug
        }

        fun info(message: String) {
            Log.i(LEVEL.INFO.value, " ${getTime()} : $message")
        }

        fun error(message: String) {
            Log.e(LEVEL.ERROR.value, " ${getTime()} : $message")
        }

        fun warning(message: String) {
            if (this.is_debug) {
                Log.w(LEVEL.WARN.value, " ${getTime()} : $message")
            }
        }

        fun success(message: String) {
            if (this.is_debug) {
                Log.i(LEVEL.SUCCESS.value, " ${getTime()} : $message")
            }
        }

        fun debug(message: String) {
            if (this.is_debug) {
                Log.d(LEVEL.DEBUG.value, " ${getTime()} : $message")
            }
        }

        private fun getTime(): String {
            return Date().toString()
        }
    }
}