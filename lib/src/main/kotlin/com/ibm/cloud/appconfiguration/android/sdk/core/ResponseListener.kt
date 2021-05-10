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

interface ResponseListener {


    /**
     * This method will be called only when a response from the server has been received with a status
     * in the 200 range.
     * @param response the server response
     */
    fun onSuccess(response: Response)

    /**
     * This method will be called in the following cases:
     *
     *  * There is no response from the server.
     *  * The status from the server response is in the 400 or 500 ranges.
     *  * There is an operational failure such as: authentication failure, data validation failure, or custom failure.
     *
     * @param response Contains detail regarding why the Http request failed. May be null if the request did not reach the server
     * @param throwable Exception that could have caused the request to fail. null if no Exception thrown.
     * @param extendedInfo Contains details regarding operational failure. null if no operational failure occurred.
     */
    fun onFailure(
        response: Response?,
        throwable: Throwable?,
        extendedInfo: JSONObject?
    )
}