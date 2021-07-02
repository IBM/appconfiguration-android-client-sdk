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

import com.ibm.cloud.appconfiguration.android.sdk.AppConfiguration

/**
 * Class consisting of methods that constructs all the URL's required by the SDK.
 */
internal class URLBuilder {

    private val _path = "/feature/v1/instances/"
    private val _service = "/apprapp"
    private val _events = "/events/v1/instances/"
    private var httpBase = ""

    companion object Factory {
        private var instance: URLBuilder? = null

        /**
         * @return instance of [URLBuilder]
         */
        fun getInstance(): URLBuilder {
            if (instance == null)
                instance =
                    URLBuilder()
            return instance!!
        }
    }

    /**
     * Initialize values
     *
     * @param collectionId collection id
     * @param environmentId environment id
     */
    fun init(collectionId: String, environmentId: String) {
        httpBase = ConfigConstants.DEFAULT_HTTP_TYPE
        if (AppConfiguration.overrideServerHost != null) {
            httpBase = AppConfiguration.overrideServerHost!!
        } else {
            httpBase += AppConfiguration.getInstance().getRegion()
            httpBase += ConfigConstants.DEFAULT_BASE_URL
        }
        httpBase += _service + _path + AppConfiguration.getInstance()
            .getGuid() + "/collections/" + collectionId + "/config?environment_id=" + environmentId
    }

    /**
     * Return the Configuration URL.
     *
     * @return configuration url
     */
    fun getConfigUrl(): String {
        return httpBase
    }

    /**
     * Return the metering URL.
     *
     * @param instanceGuid guid of App Configuration service instance
     * @return metering url
     */
    fun getMeteringUrl(instanceGuid: String): String {

        var base = ConfigConstants.DEFAULT_HTTP_TYPE
        if (AppConfiguration.overrideServerHost != null) {
            base = AppConfiguration.overrideServerHost.toString() + _service
        } else {
            base += AppConfiguration.getInstance()
                .getRegion() + ConfigConstants.DEFAULT_BASE_URL + _service
        }
        return "$base$_events$instanceGuid/usage"
    }

}