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

package com.ibm.appconfiguration.android.lib.core

object Constants {
    const val INIT_ERROR = "Error in initialising App Configuration SDK."
    const val REGION_ERROR = INIT_ERROR + "Provide a valid region."
    const val GUID_ERRROR = INIT_ERROR + "Provide a valid guid."
    const val APIKEY_ERROR = INIT_ERROR + "Provide a valid apikey."
    const val COLLECTIONID_ERROR = "Invalid action in AppConfiguration. This action can be performed only after a successful initialization. Please check the initialization section for errors."
    const val COLLECTION_SUB_ERROR = "Invalid action in AppConfiguration. This action can be performed only after a successful initialization and set collections ID value operation. Please check the initialization and setCollectionId sections for errors."
    const val IDENTITY_UPDATE_ERROR = "An `IdentityId` value should be passed for this method."

}

