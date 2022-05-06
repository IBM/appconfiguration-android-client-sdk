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

import kotlin.math.pow

/**
 * Constants used by the SDK
 */
object ConfigConstants {
    const val DEFAULT_SEGMENT_ID = "\$\$null\$\$"
    const val DEFAULT_ENTITY_ID = "\$\$null\$\$"
    const val REQUEST_SUCCESS_200 = 200
    const val REQUEST_SUCCESS_299 = 299
    const val DEFAULT_HTTP_TYPE = "https://"
    const val DEFAULT_BASE_URL = ".apprapp.cloud.ibm.com"
    const val DEFAULT_IAM_DEV_STAGE_URL = "iam.test.cloud.ibm.com"
    const val SERVICE_NAME = "AppConfiguration"
    const val DEFAULT_USAGE_LIMIT = 10
    const val SEED = 0
    const val OFFSET = 0
    val MAX_VAL = 2.0.pow(32.0)
    const val DEFAULT_ROLLOUT_PERCENTAGE = 100
    const val ROLLOUT_PERCENTAGE = "rollout_percentage"
    const val IS_ENABLED = "is_enabled"
    const val CURRENT_VALUE = "current_value"
    const val VALUE = "value"
    const val FEATURE_ENABLED = "feature_enabled"
    const val EVALUATED_SEGMENT_ID = "evaluated_segment_id";
}