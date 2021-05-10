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

import android.app.Application
import com.ibm.cloud.appconfiguration.android.sdk.core.Logger
import com.ibm.cloud.appconfiguration.android.sdk.configurations.internal.ConfigMessages
import com.ibm.cloud.appconfiguration.android.sdk.configurations.ConfigurationHandler
import com.ibm.cloud.appconfiguration.android.sdk.configurations.ConfigurationUpdateListener
import com.ibm.cloud.appconfiguration.android.sdk.configurations.internal.Validators
import com.ibm.cloud.appconfiguration.android.sdk.configurations.models.Feature
import com.ibm.cloud.appconfiguration.android.sdk.configurations.models.Property
import java.util.HashMap


/**
 * Single access point to all AppConfiguration SDKs from Kotlin.
 * Acts as a single point of contact for all the AppConfiguration extensions.
 */
class AppConfiguration {

    /** Private variables*/

    private var application: Application? = null
    private var region: String = AppConfiguration.REGION_US_SOUTH
    private var apikey: String? = null
    private var guid: String? = null
    private var isInitializedConfig = false
    private var isInitialized = false
    private var configurationHandlerInstance: ConfigurationHandler? = null

    companion object {
        private var instance: AppConfiguration? = null

        const val REGION_US_SOUTH = "us-south"
        const val REGION_EU_GB = "eu-gb"
        const val REGION_AU_SYD = "au-syd"

        @JvmField
        var overrideServerHost: String? = null

        /**
         * Returns the default AppConfiguration instance.
         */
        @JvmStatic fun getInstance(): AppConfiguration {
            if (instance == null)
                instance = AppConfiguration()
            return instance!!
        }
    }

    /**
     * Set the common values of AppConfiguration.
     * @param application   Application reference
     * @param region        AppConfiguration instance region.
     * @param guid          AppConfiguration instance GUID.
     * @param apikey        AppConfiguration instance apikey.
     */
    fun init(application: Application, region: String, guid: String, apikey: String) {

        if (!Validators.validateString(region)) {
            Logger.error(ConfigMessages.REGION_ERROR)
            return
        }

        if (!Validators.validateString(guid)) {
            Logger.error(ConfigMessages.GUID_ERROR)
            return
        }

        if (!Validators.validateString(apikey)) {
            Logger.error(ConfigMessages.API_KEY_ERROR)
            return
        }

        this.apikey = apikey
        this.application = application
        this.region = region
        this.guid = guid
        this.isInitialized = true
    }

    /** Method to get the current application */
    internal fun getApplication(): Application? {
        return this.application
    }

    /** Method to get the current AppConfiguration instance region */
    internal fun getRegion(): String {
        return region
    }

    /** Method to get the current AppConfiguration instance GUID */
    internal fun getGuid(): String {
        return guid ?: ""
    }

    /** Method to get the current AppConfiguration instance apikey */
    internal fun getApikey(): String {
        return apikey ?: ""
    }

    // MARK: Configuration Section

    /**
     * Set the AppConfiguration instance collectionId and environmentId to get the configurations
     * @param collectionId   AppConfiguration instance collectionId.
     * @param environmentId   AppConfiguration instance environmentId.
     */
    fun setContext(collectionId: String, environmentId: String ) {

        if (!isInitialized) {
            Logger.error(ConfigMessages.COLLECTION_ID_ERROR)
            return
        }

        if (!Validators.validateString(collectionId)) {
            Logger.error(ConfigMessages.COLLECTION_ID_VALUE_ERROR)
            return
        }

        if (!Validators.validateString(environmentId)) {
            Logger.error(ConfigMessages.ENVIRONMENT_ID_VALUE_ERROR)
            return
        }
        this.isInitializedConfig = true
        configurationHandlerInstance = ConfigurationHandler.getInstance()
        configurationHandlerInstance?.init(this.application!!.applicationContext, collectionId, environmentId)
        configurationHandlerInstance?.fetchConfigurations()
    }

    /**
     * Reload the AppConfiguration configurations
     */
    fun fetchConfigurations() {
        if (this.isInitializedConfig && configurationHandlerInstance != null) {
            configurationHandlerInstance?.fetchConfigurations()
        } else {
            Logger.error(ConfigMessages.COLLECTION_INIT_ERROR)
        }
    }

    /**
     * Set the listener for AppConfiguration configurations update
     * @param listener  ConfigurationUpdateListener instance.
     */
    fun registerConfigurationUpdateListener(listener: ConfigurationUpdateListener) {
        if (this.isInitializedConfig && configurationHandlerInstance != null) {
            configurationHandlerInstance?.registerConfigurationUpdateListener(listener)
        } else {
            Logger.error(ConfigMessages.COLLECTION_INIT_ERROR)
        }
    }

    /**
     * Get a feature with the featureId.
     * @param featureId  Feature id value.
     */
    fun getFeature(featureId: String): Feature? {
        return if (this.isInitializedConfig && configurationHandlerInstance != null) {
            configurationHandlerInstance?.getFeature(featureId)
        } else {
            Logger.error(ConfigMessages.COLLECTION_INIT_ERROR)
            null
        }
    }

    /** Get all the features */
    fun getFeatures(): HashMap<String, Feature>? {
        return if (this.isInitializedConfig && configurationHandlerInstance != null) {
            configurationHandlerInstance?.getFeatures()
        } else {
            Logger.error(ConfigMessages.COLLECTION_INIT_ERROR)
            null
        }
    }

    /** Get all the Properties */
    fun getProperties(): HashMap<String, Property>? {
        return if (this.isInitializedConfig && configurationHandlerInstance != null) {
            configurationHandlerInstance?.getProperties()
        } else {
            Logger.error(ConfigMessages.COLLECTION_INIT_ERROR)
            null
        }
    }

    /**
     * Get a Property with the propertyId.
     * @param propertyId  Property id value.
     */
    fun getProperty(propertyId: String?): Property? {
        return if (this.isInitializedConfig && configurationHandlerInstance != null) {
            configurationHandlerInstance?.getProperty(propertyId)
        } else {
            Logger.error(ConfigMessages.COLLECTION_INIT_ERROR)
            null
        }
    }

    /** Enable or Disable the logging*/
    fun enableDebug(enable: Boolean) {
        Logger.setDebug(enable)
    }
}