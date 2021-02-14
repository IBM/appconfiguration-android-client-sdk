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

package com.ibm.appconfiguration.android.lib

import android.app.Application
import com.ibm.appconfiguration.android.lib.core.Logger
import com.ibm.appconfiguration.android.lib.core.Constants
import com.ibm.appconfiguration.android.lib.feature.FeatureHandler
import com.ibm.appconfiguration.android.lib.feature.FeaturesUpdateListener
import com.ibm.appconfiguration.android.lib.feature.internal.Validators
import com.ibm.appconfiguration.android.lib.feature.models.Feature
import org.json.JSONObject
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
    private var guid: String? = null;
    private var isInitializedFeature = false
    private var isInitialized = false
    private var featureHandlerInstance: FeatureHandler? = null

    companion object {
        private var instance: AppConfiguration? = null

        @JvmField
        val REGION_US_SOUTH = "us-south"

        @JvmField
        val REGION_EU_GB = "eu-gb"

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
            Logger.error(Constants.REGION_ERROR)
            return
        }

        if (!Validators.validateString(guid)) {
            Logger.error(Constants.GUID_ERRROR)
            return
        }

        if (!Validators.validateString(apikey)) {
            Logger.error(Constants.APIKEY_ERROR)
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
        return this.application;
    }

    /** Method to get the current AppConfiguration instance region */
    internal fun getRegion(): String {
        return region;
    }

    /** Method to get the current AppConfiguration instance GUID */
    internal fun getGuid(): String {
        return guid ?: "";
    }

    /** Method to get the current AppConfiguration instance apikey */
    internal fun getApikey(): String {
        return apikey ?: "";
    }

    // MARK: Feature Section

    /**
     * Set the AppConfiguration instance collectionId to get the features
     * @param collectionId   AppConfiguration instance collectionId.
     */
    fun setCollectionId(collectionId: String) {

        if (!Validators.validateString(collectionId)) {
            Logger.error("Provide a valid collectionId AppConfiguration init")
            return
        }

        if (!isInitialized) {
            Logger.error(Constants.COLLECTIONID_ERROR)
            return
        }

        featureHandlerInstance = FeatureHandler.getInstance()
        featureHandlerInstance?.init(this.application!!.applicationContext, collectionId)
        featureHandlerInstance?.fetchFeaturesData()
        this.isInitializedFeature = true
    }

    /**
     * Reload the AppConfiguration features
     */
    fun fetchFeatureData() {
        if (this.isInitializedFeature && featureHandlerInstance != null) {
            featureHandlerInstance?.fetchFeaturesData()
        } else {
            Logger.error(Constants.COLLECTION_SUB_ERROR)
        }
    }

    /**
     * Set the listener for AppConfiguration features
     * @param listener  FeaturesUpdateListener instance.
     */
    fun registerFeaturesUpdateListener(listener: FeaturesUpdateListener) {
        if (this.isInitializedFeature && featureHandlerInstance != null) {
            featureHandlerInstance?.registerFeaturesUpdateListener(listener)
        } else {
            Logger.error(Constants.COLLECTION_SUB_ERROR)
        }
    }

    /**
     * Get a feature with the featureId.
     * @param featureId  Feature id value.
     */
    fun getFeature(featureId: String): Feature? {
        return if (this.isInitializedFeature && featureHandlerInstance != null) {
            featureHandlerInstance?.getFeature(featureId)
        } else {
            Logger.error(Constants.COLLECTION_SUB_ERROR)
            null
        }
    }

    /** Get all the features */
    fun getFeatures(): HashMap<String, Feature>? {
        return if (this.isInitializedFeature && featureHandlerInstance != null) {
            featureHandlerInstance?.getFeatures()
        } else {
            Logger.error(Constants.COLLECTION_SUB_ERROR)
            null
        }
    }

    /** Enable or Disable the logging*/
    fun enableDebug(enable: Boolean) {
        Logger.setDebug(enable)
    }
}