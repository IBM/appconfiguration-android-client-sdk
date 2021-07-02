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

/**
 * Logger messages used by the SDK
 */
object ConfigMessages {
    private const val INIT_ERROR = "Error in initialising App Configuration SDK."
    const val CONFIG_HANDLER_INIT_ERROR =
        "Invalid action in ConfigurationHandler. This action can be performed only after a successful initialization. Please check the initialization section for errors."
    const val REGION_ERROR = INIT_ERROR + "Provide a valid region."
    const val GUID_ERROR = INIT_ERROR + "Provide a valid guid."
    const val API_KEY_ERROR = INIT_ERROR + "Provide a valid apikey."
    const val ENVIRONMENT_ID_VALUE_ERROR =
        "Provide a valid environmentId in App Configuration setContext method"
    const val COLLECTION_ID_ERROR =
        "Invalid action in AppConfiguration. This action can be performed only after a successful initialization. Please check the initialization section for errors."
    const val COLLECTION_INIT_ERROR =
        "Invalid action in AppConfiguration. This action can be performed only after a successful initialization and setContext operation. Please check the initialization and setContext sections for errors."
    const val ENTITY_UPDATE_ERROR = "An `entityId` value should be passed for this method."
    const val FEATURE_INVALID = "Invalid featureId - "
    const val PROPERTY_INVALID = "Invalid propertyId - "
    const val CONFIG_API_ERROR =
        "Invalid configuration. Verify the collectionId, environmentId, apikey, guid and region."
    const val COLLECTION_ID_VALUE_ERROR =
        "Provide a valid collectionId in App Configuration setContext method"
    const val APPLICATION_CONTEXT_ERROR = "Provide a valid application context"

}

