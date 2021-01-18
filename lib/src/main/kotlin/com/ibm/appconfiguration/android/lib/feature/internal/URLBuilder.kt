
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

import com.ibm.appconfiguration.android.lib.AppConfiguration


internal class URLBuilder {

    val _baseUrl = ".apprapp.cloud.ibm.com"
    val wsUrl = "/wsfeature"
    val _path = "/feature/v1/instances/"
    val _service = "/apprapp"
    var httpBase = "https://"
    val _events = "/events/v1/instances/"
    var reWriteDomain: String? = null

    companion object Factory {
        private var instance: URLBuilder? = null
        fun getInstance(): URLBuilder {
            if (instance == null)
                instance =
                    URLBuilder()
            return instance!!
        }
    }

    fun init(collectionId: String) {
        if (AppConfiguration.overrideServerHost != null) {
            httpBase = ""
            httpBase += AppConfiguration.overrideServerHost
            reWriteDomain = AppConfiguration.overrideServerHost
        } else {
            httpBase = "https://"
            httpBase += AppConfiguration.getInstance().getRegion()
            httpBase += _baseUrl
            reWriteDomain = ""
        }
        httpBase += _service + _path + AppConfiguration.getInstance()
            .getGuid() + "/collections/" + collectionId + "/config"
    }

    fun getConfigUrl(): String {
        return httpBase
    }

    fun getMeteringurl(instanceGuid: String): String? {
        var base = "https://" + AppConfiguration.getInstance().getRegion() + _baseUrl + _service
        if (AppConfiguration.overrideServerHost != null) {
            base = AppConfiguration.overrideServerHost.toString() + _service
        }
        return "$base$_events$instanceGuid/usage"
    }

}