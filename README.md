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

- Configure the Module level `build.gradle` and App level `build.gradle` files.

  1. Add IBM Cloud AppConfiguration Android client SDK dependency to Project level `build.gradle` file.

        ```kt
        repositories {
            mavenCentral()
        }
        ```


  2. Add IBM Cloud AppConfiguration Android client SDK dependency to Module level `build.gradle` file.

        ```kt
        dependencies {
	        implementation "com.ibm.cloud:appconfiguration-android-sdk:2.0.0"
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
                          AppConfiguration.REGION_US_SOUTH,
                          "guid",
                          "apikey")

    //To start the configuration fetching operation, set the collectionId and environmentId in the following way.
    appConfiguration.setContext("collectionId","environmentId")

 
```

- region : Region name where the service instance is created. Use
    - `AppConfiguration.REGION_US_SOUTH` for Dallas
    - `AppConfiguration.REGION_EU_GB` for London
    - `AppConfiguration.REGION_AU_SYD` for Sydney
- guid : GUID of the App Configuration service. Get it from the service instance credentials section of the dashboard
- apikey : ApiKey of the App Configuration service. Get it from the service instance credentials section of the dashboard
- collectionId : Id of the collection created in App Configuration service instance under the **Collections** section.
- environmentId : Id of the environment created in App Configuration service instance under the **Environments** section.

## Set listener for feature or property data changes

```kt

appConfiguration.registerConfigurationUpdateListener(object : ConfigurationUpdateListener {

    override fun onConfigurationUpdate() {
        // ADD YOUR CODE
    }
})

```

## Get single feature

```kt
val feature: Feature? = appConfiguration.getFeature("featureId")
```

## Get all features

```kt
val features: HashMap<String, Feature>? = appConfiguration.getFeatures();
```

## Feature evaluation

```kt

val identityAttributes = JSONObject()
try {
    identityAttributes.put("city", "Bangalore")
    identityAttributes.put("country", "India")
} catch (e: JSONException) {
    e.printStackTrace()
}


val appConfiguration = AppConfiguration.getInstance()
val feature: Feature? = appConfiguration.getFeature("featureId")

if (feature?.getFeatureDataType() === Feature.FeatureType.NUMERIC) {

    val value = feature.getCurrentValue("identityId", identityAttributes)

} else if (feature?.getFeatureDataType() === Feature.FeatureType.BOOLEAN) {

    val value = feature.getCurrentValue("identityId", identityAttributes)

} else if (feature?.getFeatureDataType() === Feature.FeatureType.STRING) {

    val value = feature.getCurrentValue("identityId", identityAttributes)

}
```

## Get single Property

```kt
val property: Property? = appConfiguration.getProperty("propertyId")
```

## Get all Properties

```kt
val properties: HashMap<String, Property>? = appConfiguration.getProperties();
```

## Property evaluation

```kt

JSONObject identityAttributes = new JSONObject();
try {
    identityAttributes.put("city", "Bangalore");
    identityAttributes.put("country", "India");
} catch (JSONException e) {
    e.printStackTrace();
}


val appConfiguration = AppConfiguration.getInstance()
val property: Property? = appConfiguration.getProperty("propertyId")
val value = property.getCurrentValue("identityId", identityAttributes)
```

## Enable/Disable Logger (optional)

```kt
val appConfiguration = AppConfiguration.getInstance()

// Enable Logger 

appConfiguration.enableDebug(true)

// Disable Logger

appConfiguration.enableDebug(false)

```

## Force fetch the configurations from server


```kt
appConfiguration.fetchConfigurations()
```


# Java

## Installation Java

Choose to integrate the AppConfiguration Android client SDK package using either of the following options:

- Download and import the package to your Android Studio project
- Get the package through Gradle

## Initialize Java SDK

- Configure the Module level `build.gradle` and App level `build.gradle` files.

  1. Add IBM Cloud AppConfiguration Android client SDK dependency to Project level `build.gradle` file.

        ```java
        repositories {
            mavenCentral()
        }
        ```
  2. Add IBM Cloud AppConfiguration Android client SDK dependency to Module level `build.gradle` file.

        ```java
        dependencies {
	        implementation "com.ibm.cloud:appconfiguration-android-sdk:2.0.0"
	    }
        ```
    
- Configure the `AndroidManifest.xml` file for `Internet` permission. 
    
    ```java
    	 <uses-permission android:name="android.permission.INTERNET"/>
    ```
### integrating Kotlin to Java project

- Add the Kotlin gradle pluign to the Module level `build.gradle`

    ```kt
    dependencies {
            classpath "com.android.tools.build:gradle:4.1.1"
            classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
    ```
- Add `kotlin-android` plugin to the App level `build.gradle`

    ```kt
    plugins {
        id 'com.android.application'
        id 'kotlin-android'
    }
    ```
## Import AppConfiguration Java SDK

```java
      AppConfiguration appConfiguration = AppConfiguration.getInstance();
      appConfiguration.init(getApplication(), AppConfiguration.REGION_US_SOUTH, "guid", "apikey");

      // To start the configuration fetching operation, set the collectionId and environmentId in the following way.
      appConfiguration.setContext("collectionId", "environmentId");
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
Feature feature = appConfiguration.getFeature("featureId");
```

## Get all feature

```java
HashMap<String,Feature> features =  appConfiguration.getFeatures();

```

## Feature evaluation

```java

JSONObject identityAttributes = new JSONObject();

try {
    identityAttributes.put("city", "Bengaluru");
    identityAttributes.put("country", "India");
} catch (JSONException e) {
    e.printStackTrace();
}

AppConfiguration appConfiguration = AppConfiguration.getInstance();
Feature feature = appConfiguration.getFeature("featureId")
if(feature != null) 
    switch (feature.getFeatureDataType())
        case STRING:
            String value = (String) feature.getCurrentValue(identityId, identityAttributes);
            System.out.println(value);
            break;
        case BOOLEAN:
            Boolean boolVal = (Boolean) feature.getCurrentValue(identityId, identityAttributes);
            System.out.println(boolVal);
            break;
        case NUMERIC:
            Integer intVal = (Integer) feature.getCurrentValue(identityId, identityAttributes);
            System.out.println(intVal);
            break;
    }
}
```

## Get single Property

```java
Property property = appConfiguration.getProperty("propertyId");

```

## Get all Properties

```java
HashMap<String,Property> properties =  appConfiguration.getProperties();

```

## Property evaluation

```java

JSONObject identityAttributes = new JSONObject();

try {
    identityAttributes.put("city", "Bengaluru");
    identityAttributes.put("country", "India");
} catch (JSONException e) {
    e.printStackTrace();
}


AppConfiguration appConfiguration = AppConfiguration.getInstance();
Property property = appConfiguration.getProperty("propertyId")
String value = (String) property.getCurrentValue(identityId, identityAttributes);
```

## Enable/Disable Logger (optional)

```Java
AppConfiguration appConfiguration = AppConfiguration.getInstance();

// Enable Logger 
appConfiguration.enableDebug(true);


// Disable Logger

appConfiguration.enableDebug(false);

```

## Force fetch the configurations from server

```Java
appConfiguration.fetchConfigurations()
```

## License

This project is released under the Apache 2.0 license. The license's full text can be found in [LICENSE](https://github.com/IBM/appconfiguration-android-client-sdk/blob/master/LICENSE)
