# App Configuration Android client SDK


Repository for IBM Cloud App Configuration Android client SDK

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
            jcenter()
        }
        ```


  2. Add IBM Cloud AppConfiguration Android client SDK dependency to Module level `build.gradle` file.

        ```kt
        dependencies {
	        implementation "com.ibm.appconfiguration.android:lib:1.1.0"
	        implementation "com.squareup.okhttp3:okhttp:4.9.0"
	        implementation "com.squareup.okhttp3:okhttp-urlconnection:4.9.0"
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

    //To start the feature fetching operation, set the collectioId in the following way.
     appConfiguration.setCollectionId("collection_id")
 
```

  - region : Region name where the service instance is created. Eg: `AppConfiguration.REGION_US_SOUTH`.
  - guid : GUID of the App Configuration service. Get it from the service credentials section of the dashboard.
  - apikey : ApiKey of the App Configuration service. Get it from the service credentials section of the dashboard.
  - collection_id : Id of the collection created in App Configuration service instance.

## Listen to the feature changes.

```kt

appConfiguration.registerFeaturesUpdateListener(object : FeaturesUpdateListener {

    override fun onFeaturesUpdate() {
        // ADD YOUR CODE
    }
})

```

## Get single feature

```kt
val feature: Feature? = appConfiguration.getFeature("featureId")
```

## Get all feature

```kt
val features: HashMap<String, Feature>? = appConfiguration.getFeatures();
```

## Feature evaluation

```kt

JSONObject identityAttributes = new JSONObject();
try {
    identityAttributes.put("city", "Bangalore");
    identityAttributes.put("country", "India");
} catch (JSONException e) {
    e.printStackTrace();
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

## Enable/Disable Logger

```kt
val appConfiguration = AppConfiguration.getInstance()

// Enable Logger 

appConfiguration.enableDebug(true)

// Disable Logger

appConfiguration.enableDebug(false)

```

## Force fetch the features from server


```kt
appConfiguration.fetchFeatureData()
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
            jcenter()
        }
        ```
  2. Add IBM Cloud AppConfiguration Android client SDK dependency to Module level `build.gradle` file.

        ```java
        dependencies {
	        implementation "com.ibm.appconfiguration.android:lib:1.1.0"
	        implementation "com.squareup.okhttp3:okhttp:4.9.0"
	        implementation "com.squareup.okhttp3:okhttp-urlconnection:4.9.0"
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

      // To start the feature fetching operation, set the collectioId in the following way.
      appConfiguration.setCollectionId("collection_id");
```

  - region : Region name where the service instance is created. Eg: `AppConfiguration.REGION_US_SOUTH`.
  - guid : GUID of the App Configuration service. Get it from the service credentials section of the dashboard.
  - apikey : ApiKey of the App Configuration service. Get it from the service credentials section of the dashboard.
  - collection_id : Id of the collection created in App Configuration service instance.


## Listen to the feature changes.

```java

appConfiguration.registerFeaturesUpdateListener(new FeaturesUpdateListener() {
    @Override
    public void onFeaturesUpdate() {
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

## Enable/Disable Logger

```Java
AppConfiguration appConfiguration = AppConfiguration.getInstance();

// Enable Logger 
appConfiguration.enableDebug(true);


// Disable Logger

appConfiguration.enableDebug(false);

```

## Force fetch the features from server

```Java
appConfiguration.fetchFeatureData()
```

## License

This project is released under the Apache 2.0 license. The license's full text can be found in [LICENSE](https://github.com/IBM/appconfiguration-android-client-sdk/blob/master/LICENSE)
