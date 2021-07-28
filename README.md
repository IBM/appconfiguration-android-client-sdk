# IBM Cloud App Configuration Android client SDK

IBM Cloud App Configuration SDK is used to perform feature flag and property evaluation based on the configuration on IBM Cloud App Configuration service.

## Overview

IBM Cloud App Configuration is a centralized feature management and configuration service on [IBM Cloud](https://www.cloud.ibm.com) for use with web and mobile applications, microservices, and distributed environments.

Instrument your applications with App Configuration Android SDK, and use the App Configuration dashboard, CLI or API to define feature flags or properties, organized into collections and targeted to segments. Toggle feature flag states in the cloud to activate or deactivate features in your application or environment, when required. You can also manage the properties for distributed applications centrally.

## Contents
- [Prerequisites](#prerequisites)
- [Kotlin](#kotlin)
    - [Installation](#installation-kotlin)
    - [Initialize SDK](#initialize-kotlin-sdk)
    - [Import Kotlin AppConfiguration SDK](#import-kotlin-appconfiguration-sdk)
- [Java](#java)
    - [Installation](#installation-java)
    - [Initialize SDK](#initialize-java-sdk)
    - [Import Java AppConfiguration SDK](#import-appconfiguration-java-sdk)
- [License](#license)


### Prerequisites

* Android API level 22 or later
* [Android Studio](https://developer.android.com/studio/index.html)
* [Gradle](https://gradle.org/install)

 Follow the below step


# Kotlin

## Installation Kotlin

Choose to integrate the AppConfiguration Android client SDK package using either of the following options:

- Download and import the package to your Android Studio project
- Get the package through Gradle

## Initialize Kotlin SDK

- Configure the Module level `build.gradle` and Project level `build.gradle` files.

  1. Add IBM Cloud AppConfiguration Android client SDK dependency to Project level `build.gradle` file.

        ```kt
        repositories {
            mavenCentral()
        }
        ```


  2. Add IBM Cloud AppConfiguration Android client SDK dependency to Module level `build.gradle` file.

        ```kt
        dependencies {
	        implementation "com.ibm.cloud:appconfiguration-android-sdk:0.1.2"
	    }
        ```
        
    
- Configure the `AndroidManifest.xml` file for `Internet` permission. 
    
    ```kt
    	 <uses-permission android:name="android.permission.INTERNET"/>
    ```

## Import Kotlin AppConfiguration SDK

  ### initialization
  ```kt
    val appConfiguration = AppConfiguration.getInstance()

    appConfiguration.init( application,
                          region,
                          "apikey",
                          "guid")

    //To start the configuration fetching operation, set the collectionId and environmentId in the following way.
    val collectionId = "airlines-webapp"
    val environmentId = "dev"
    appConfiguration.setContext(collectionId, environmentId)

 
```

- region : Region name where the service instance is created. Use
    - `AppConfiguration.REGION_US_SOUTH` for Dallas
    - `AppConfiguration.REGION_EU_GB` for London
    - `AppConfiguration.REGION_AU_SYD` for Sydney
- guid : GUID of the App Configuration service. Get it from the service instance credentials section of the dashboard
- apikey : ApiKey of the App Configuration service. Get it from the service instance credentials section of the dashboard
- collectionId : Id of the collection created in App Configuration service instance under the **Collections** section.
- environmentId : Id of the environment created in App Configuration service instance under the **Environments** section.

## Set listener for the feature and property data changes

To listen to the configuration changes in your App Configuration service instance, implement the `registerConfigurationUpdateListener` event listener as mentioned below.

```kt

appConfiguration.registerConfigurationUpdateListener(object : ConfigurationUpdateListener {
    override fun onConfigurationUpdate() {
        println("Received updates on configurations")
    }
})

```

## Get single feature

```kt
val feature: Feature? = appConfiguration.getFeature("online-check-in")
```

## Get all features

```kt
val features: HashMap<String, Feature>? = appConfiguration.getFeatures()
```

## Evaluate a feature

Use the `feature.getCurrentValue(entity_id, entity_attributes)` method to evaluate the value of the feature flag. Pass an unique entityId as the parameter to perform the feature flag evaluation.

### Usage

  - If the feature flag is configured with segments in the AppConfiguration service, provide a json object as entityAttributes parameter to this method.

    ```kt
    val entityId = "john_doe"
    val entityAttributes = JSONObject()
    
    try {
        entityAttributes.put("city", "Bangalore")
        entityAttributes.put("country", "India")
    } catch (e: JSONException) {
        e.printStackTrace()
    }
   
    val appConfiguration = AppConfiguration.getInstance()
    val feature: Feature? = appConfiguration.getFeature("online-check-in")
    
    val value = feature.getCurrentValue(entityId, entityAttributes)

    ```

  - If the feature flag is not targeted to any segments and the feature flag is turned ON this method returns the feature enabled value. And when the feature flag is turned OFF this method returns the feature disabled value.

    ```py
    val entityId = "john_doe"
    val value = feature.getCurrentValue(entityId)
    ```

## Get single Property

```kt
val property: Property? = appConfiguration.getProperty("check-in-charges")
```

## Get all Properties

```kt
val properties: HashMap<String, Property>? = appConfiguration.getProperties()
```

## Evaluate a property

Use the `property.getCurrentValue(entity_id, entity_attributes)` method to evaluate the value of the property. Pass an unique entityId as the parameter to perform the property evaluation.

### Usage

- If the property is configured with segments in the App Configuration service, provide a json object as entityAttributes parameter to this method.

    ```kt
        val entityId = "john_doe"
        val entityAttributes = JSONObject()

        try {
            entityAttributes.put("city", "Bangalore")
            entityAttributes.put("country", "India")
        } catch (e: JSONException) {
            e.printStackTrace()
        }


        val appConfiguration = AppConfiguration.getInstance()
        val property: Property? = appConfiguration.getProperty("check-in-charges")
        val value = property.getCurrentValue(entityId, entityAttributes)
    ```

- If the property is not targeted to any segments this method returns the property value.

    ```kt
    val entityId = "john_doe"
    val property: Property? = appConfiguration.getProperty("check-in-charges")
    val value = property.getCurrentValue(entityId)
    ```

## Enable/Disable Logger (optional)

Use this method to enable/disable the logging in SDK.

```kt
val appConfiguration = AppConfiguration.getInstance()

// Enable Logger 
appConfiguration.enableDebug(true)

// Disable Logger
appConfiguration.enableDebug(false)

```

## Force fetch the configurations from server

Fetch the latest configuration data. 

```kt
appConfiguration.fetchConfigurations()
```


# Java

## Installation Java

Choose to integrate the AppConfiguration Android client SDK package using either of the following options:

- Download and import the package to your Android Studio project
- Get the package through Gradle

## Initialize Java SDK

- Configure the Module level `build.gradle` and Project level `build.gradle` files.

  1. Add IBM Cloud AppConfiguration Android client SDK dependency to Project level `build.gradle` file.

        ```java
        repositories {
            mavenCentral()
        }
        ```
  2. Add IBM Cloud AppConfiguration Android client SDK dependency to Module level `build.gradle` file.

        ```java
        dependencies {
	        implementation "com.ibm.cloud:appconfiguration-android-sdk:0.1.2"
	    }
        ```
    
- Configure the `AndroidManifest.xml` file for `Internet` permission. 
    
    ```java
    	 <uses-permission android:name="android.permission.INTERNET"/>
    ```
### integrating Kotlin to Java project

- Add the Kotlin gradle plugin to the Project level `build.gradle`

    ```kt
    dependencies {
            classpath "com.android.tools.build:gradle:4.1.1"
            classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
    ```

- Add the following in the `buildscript` section in the Project level `build.gradle` 

    ```kt
    buildscript {
        ext.kotlin_version = "1.4.31"
    }
    ```

- Add `kotlin-android` plugin to the Module level `build.gradle`

    ```kt
    plugins {
        id 'com.android.application'
        id 'kotlin-android'
    }
    ```
## Import AppConfiguration Java SDK

```java
      AppConfiguration appConfiguration = AppConfiguration.getInstance();
      appConfiguration.init(getApplication(), 
                            region, "guid", "apikey");

      // To start the configuration fetching operation, set the collectionId and environmentId in the following way.
      String collectionId = "airlines-webapp";
      String environmentId = "dev";
      appConfiguration.setContext(collectionId, environmentId);
```

- region : Region name where the service instance is created. Use
    - `AppConfiguration.REGION_US_SOUTH` for Dallas
    - `AppConfiguration.REGION_EU_GB` for London
    - `AppConfiguration.REGION_AU_SYD` for Sydney
- guid : GUID of the App Configuration service. Get it from the service instance credentials section of the dashboard
- apikey : ApiKey of the App Configuration service. Get it from the service instance credentials section of the dashboard
- collectionId : Id of the collection created in App Configuration service instance under the **Collections** section.
- environmentId : Id of the environment created in App Configuration service instance under the **Environments** section.


## Listen to the feature changes.

To listen to the configuration changes in your App Configuration service instance, implement the `registerConfigurationUpdateListener` event listener as mentioned below.

```java

appConfiguration.registerConfigurationUpdateListener(new ConfigurationUpdateListener() {
    @Override
    public void onConfigurationUpdate() {
       // ADD YOUR CODE
    }
});
```

## Get single feature

```java
Feature feature = appConfiguration.getFeature("online-check-in"); //feature can be null incase of an invalid feature id
```

## Get all feature

```java
HashMap<String,Feature> features =  appConfiguration.getFeatures();

```

## Evaluate a feature

Use the `feature.getCurrentValue(entity_id, entity_attributes)` method to evaluate the value of the feature flag. Pass an unique entityId as the parameter to perform the feature flag evaluation.

### Usage

- If the feature flag is configured with segments in the AppConfiguration service, provide a json object as entityAttributes parameter to this method.

    ```java

    String entityId = "john_doe";
    JSONObject entityAttributes = new JSONObject();

    try {
        entityAttributes.put("city", "Bengaluru");
        entityAttributes.put("country", "India");
    } catch (JSONException e) {
        e.printStackTrace();
    }

    AppConfiguration appConfiguration = AppConfiguration.getInstance();
    Feature feature = appConfiguration.getFeature("online-check-in");
    if (feature != null) {
       String value = (String) feature.getCurrentValue(entityId, entityAttributes);
    }
    ```

  - If the feature flag is not targeted to any segments and the feature flag is turned ON this method returns the feature enabled value. And when the feature flag is turned OFF this method returns the feature disabled value.


    ```java

    String entityId = "john_doe";
    AppConfiguration appConfiguration = AppConfiguration.getInstance();
    Feature feature = appConfiguration.getFeature("online-check-in");
    if (feature != null) {
       String value = (String) feature.getCurrentValue(entityId);
    }
    ```


## Get single Property

```java
Property property = appConfiguration.getProperty("check-in-charges"); //property can be null incase of an invalid property id

```

## Get all Properties

```java
HashMap<String,Property> properties =  appConfiguration.getProperties();
```

## Evaluate a property

Use the `property.get_current_value(entity_id=entity_id, entity_attributes=entity_attributes)` method to evaluate the value of the property. Pass an unique entityId as the parameter to perform the property evaluation.

### Usage

- If the property is configured with segments in the App Configuration service, provide a json object as entityAttributes parameter to this method.


    ```java

    String entityId = "john_doe";
    JSONObject entityAttributes = new JSONObject();

    try {
        entityAttributes.put("city", "Bengaluru");
        entityAttributes.put("country", "India");
    } catch (JSONException e) {
        e.printStackTrace();
    }

    AppConfiguration appConfiguration = AppConfiguration.getInstance();
    Property property = appConfiguration.getProperty("check-in-charges");

    if (property != null) {
        String value = (String) property.getCurrentValue(entityId, entityAttributes);
    }
    ```

- If the property is not targeted to any segments this method returns the property value.


    ```java

    String entityId = "john_doe";

    AppConfiguration appConfiguration = AppConfiguration.getInstance();
    Property property = appConfiguration.getProperty("check-in-charges");
    if (property != null) {
        String value = (String) property.getCurrentValue(entityId);
    }
    ```

## Enable debugger (Optional)

Use this method to enable/disable the logging in SDK.

```Java
AppConfiguration appConfiguration = AppConfiguration.getInstance();

// Enable Logger 
appConfiguration.enableDebug(true);

// Disable Logger
appConfiguration.enableDebug(false);

```

## Force fetch the configurations from server

Fetch the latest configuration data. 

```Java
appConfiguration.fetchConfigurations();
```

## Examples 

The [examples](https://github.com/IBM/appconfiguration-android-client-sdk/tree/master/app) folder has the examples. 

## License

This project is released under the Apache 2.0 license. The license's full text can be found in [LICENSE](https://github.com/IBM/appconfiguration-android-client-sdk/blob/master/LICENSE)
