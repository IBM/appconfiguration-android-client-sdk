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

import android.content.Context
import com.ibm.cloud.appconfiguration.android.sdk.configurations.ConfigurationHandler
import com.ibm.cloud.appconfiguration.android.sdk.configurations.ConfigurationUpdateListener
import com.ibm.cloud.appconfiguration.android.sdk.configurations.internal.ConfigMessages
import com.ibm.cloud.appconfiguration.android.sdk.configurations.internal.Validators
import com.ibm.cloud.appconfiguration.android.sdk.configurations.models.Feature
import com.ibm.cloud.appconfiguration.android.sdk.configurations.models.Property
import com.ibm.cloud.appconfiguration.android.sdk.core.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import java.util.*
import kotlinx.coroutines.launch

/**
 * IBM Cloud App Configuration is a centralized feature management and configuration service on IBM
 * Cloud for use with web and mobile applications, microservices, and distributed environments.
 * Instrument your applications with App Configuration Java SDK, and use the App Configuration dashboard,
 * CLI or API to define feature flags or properties, organized into collections and targeted to segments.
 * Toggle feature flag states in the cloud to activate or deactivate features in your application or
 * environment, when required. You can also manage the properties for distributed applications centrally.
 *
 * @version 0.1.2
 * @see <a href="https://cloud.ibm.com/docs/app-configuration">App Configuration</a>
 */

/**
 * Single access point to all AppConfiguration SDKs from Kotlin.
 * Acts as a single point of contact for all the AppConfiguration extensions.
 */
class AppConfiguration {

    /** Private variables*/

    private var applicationContext: Context? = null
    private var region: String = ""
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
         * Returns an instance of the [AppConfiguration] class. If the same [AppConfiguration] instance
         * is available in the cache, then that instance is returned.
         * Otherwise, a new [AppConfiguration] instance is created and cached.
         *
         * @return instance of [AppConfiguration]
         */
        @JvmStatic
        fun getInstance(): AppConfiguration {
            if (instance == null)
                instance = AppConfiguration()
            return instance!!
        }
    }

    /**
     * Set the common values of AppConfiguration.
     *
     * @param applicationContext   Application context reference
     * @param region        AppConfiguration instance region
     * @param guid          AppConfiguration instance GUID
     * @param apikey        AppConfiguration instance apikey
     */
    fun init(applicationContext: Context, region: String, guid: String, apikey: String) {

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
        this.applicationContext = applicationContext
        this.region = region
        this.guid = guid
        this.isInitialized = true
    }

    /** Method to get the current application.
     *
     * @return application context
     */
    internal fun getApplicationContext(): Context? {
        return this.applicationContext
    }

    /**
     * Method to get the current AppConfiguration instance region.
     *
     * @return region name
     */
    internal fun getRegion(): String {
        return region
    }

    /**
     * Method to get the current AppConfiguration instance GUID.
     *
     * @return guid
     */
    internal fun getGuid(): String {
        return guid ?: ""
    }

    /**
     * Method to get the current AppConfiguration instance apikey.
     *
     * @return apikey
     */
    internal fun getApikey(): String {
        return apikey ?: ""
    }

    /**
     * Set the AppConfiguration instance collectionId and environmentId to get the configurations.
     * This function also does the network call and file operations by launching a coroutine, and
     * hence is asynchronous.
     * After asynchronous activities are successfully completed, `onConfigurationUpdate()` method of
     * [ConfigurationUpdateListener] is triggered.
     * Implement the member of [ConfigurationUpdateListener] using [registerConfigurationUpdateListener] method.
     *
     * @param collectionId AppConfiguration instance collectionId
     * @param environmentId AppConfiguration instance environmentId
     */
    fun setContext(collectionId: String, environmentId: String) {

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
        if (this.applicationContext == null) {
            Logger.error(ConfigMessages.APPLICATION_CONTEXT_ERROR)
            return
        }
        this.isInitializedConfig = true
        configurationHandlerInstance = ConfigurationHandler.getInstance()
        configurationHandlerInstance?.init(this.applicationContext!!, collectionId, environmentId)
        GlobalScope.launch(Dispatchers.IO) {
            configurationHandlerInstance?.fetchConfigurations()
        }
    }

    /**
     * Reload the AppConfiguration configurations.
     *
     * This function does the network call and file operations by launching a coroutine, and
     * hence is asynchronous.
     * After asynchronous activities are successfully completed, `onConfigurationUpdate()` method of
     * [ConfigurationUpdateListener] is triggered.
     * Implement the member of [ConfigurationUpdateListener] using [registerConfigurationUpdateListener] method.
     */
    fun fetchConfigurations() {
        if (this.isInitializedConfig && configurationHandlerInstance != null) {
            GlobalScope.launch(Dispatchers.IO) {
                configurationHandlerInstance?.fetchConfigurations()
            }
        } else {
            Logger.error(ConfigMessages.COLLECTION_INIT_ERROR)
        }
    }

    /**
     * Set the listener for AppConfiguration configurations update.
     *
     * @param listener  ConfigurationUpdateListener instance
     */
    fun registerConfigurationUpdateListener(listener: ConfigurationUpdateListener) {
        if (this.isInitializedConfig && configurationHandlerInstance != null) {
            configurationHandlerInstance?.registerConfigurationUpdateListener(listener)
        } else {
            Logger.error(ConfigMessages.COLLECTION_INIT_ERROR)
        }
    }

    /**
     * Returns the [Feature] object with the details of the feature specified by the `featureId`.
     *
     * @param featureId the Feature Id
     * @return feature object
     */
    fun getFeature(featureId: String): Feature? {
        return if (this.isInitializedConfig && configurationHandlerInstance != null) {
            configurationHandlerInstance?.getFeature(featureId)
        } else {
            Logger.error(ConfigMessages.COLLECTION_INIT_ERROR)
            null
        }
    }

    /**
     * Returns all features.
     *
     * @return hashmap of all features and their corresponding [Feature] objects
     */
    fun getFeatures(): HashMap<String, Feature>? {
        return if (this.isInitializedConfig && configurationHandlerInstance != null) {
            configurationHandlerInstance?.getFeatures()
        } else {
            Logger.error(ConfigMessages.COLLECTION_INIT_ERROR)
            null
        }
    }

    /**
     * Returns all properties.
     *
     * @return hashmap of all properties and their corresponding [Property] objects
     */
    fun getProperties(): HashMap<String, Property>? {
        return if (this.isInitializedConfig && configurationHandlerInstance != null) {
            configurationHandlerInstance?.getProperties()
        } else {
            Logger.error(ConfigMessages.COLLECTION_INIT_ERROR)
            null
        }
    }

    /**
     * Returns the [Property] object with the details of the property specified by the `propertyId`.
     *
     * @param propertyId the Property Id
     * @return property object
     */
    fun getProperty(propertyId: String?): Property? {
        return if (this.isInitializedConfig && configurationHandlerInstance != null) {
            configurationHandlerInstance?.getProperty(propertyId)
        } else {
            Logger.error(ConfigMessages.COLLECTION_INIT_ERROR)
            null
        }
    }

    /**
     * Method to enable or disable the logger. By default, logger is disabled.
     *
     * @param enable boolean value `true` or `false`
     */
    fun enableDebug(enable: Boolean) {
        Logger.setDebug(enable)
    }
}