# IBM Cloud App Configuration Android client SDK

IBM Cloud App Configuration SDK is used to perform feature flag and property evaluation based on the configuration on IBM Cloud App Configuration service.

## Overview

IBM Cloud App Configuration is a centralized feature management and configuration service on [IBM Cloud](https://www.cloud.ibm.com) for use with web and mobile applications, microservices, and distributed environments.

Instrument your applications with App Configuration Android SDK, and use the App Configuration dashboard, CLI or API to define feature flags or properties, organized into collections and targeted to segments. Toggle feature flag states in the cloud to activate or deactivate features in your application or environment, when required. You can also manage the properties for distributed applications centrally.

## Contents
- [Prerequisites](#prerequisites)
- [Kotlin](#kotlin)
    - [Installation](#installation)
    - [Import SDK](#import-sdk)
    - [Initialize AppConfiguration SDK](#initialize-appconfiguration-sdk)
- [Java](#java)
    - [Installation](#installation-1)
    - [Import SDK](#import-sdk-1)
    - [Initialize AppConfiguration SDK](#initialize-appconfiguration-sdk-1)
- [License](#license)


### Prerequisites

* Android API level 22 or later
* [Android Studio](https://developer.android.com/studio/index.html)
* [Gradle](https://gradle.org/install)

 Follow the below step


# Kotlin

## Installation

Choose to integrate the AppConfiguration Android client SDK package using either of the following options:

- Download and import the package to your Android Studio project
- Get the package through Gradle

## Import SDK

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
	        implementation "com.ibm.cloud:appconfiguration-android-sdk:0.3.4"
	    }
        ```
        
    
- Configure the `AndroidManifest.xml` file for `Internet` permission. 
    
    ```kt
    	 <uses-permission android:name="android.permission.INTERNET"/>
    ```

## Initialize AppConfiguration SDK


```kt
import com.ibm.cloud.appconfiguration.android.sdk.AppConfiguration

val collectionId = "airlines-webapp"
val environmentId = "dev"

val appConfigClient = AppConfiguration.getInstance()
//application is a member of the AppCompatActivity() class, if you have inherited the 
//AppCompatActivity() class then you can call the application variable.
appConfigClient.init(application,
                      "region",
                      "guid",
                      "apikey")
appConfigClient.setContext(collectionId, environmentId)
```

:red_circle: **Important** :red_circle:

The **`init()`** and **`setContext()`** are the initialisation methods and should be invoked **only
once** using appConfigClient. The appConfigClient, once initialised, can be obtained across modules
using **`AppConfiguration.getInstance()`**.  [See this example below](#fetching-the-appconfigclient-across-other-modules).

- region : Region name where the App Configuration service instance is created. See list of
  supported locations [here](https://cloud.ibm.com/catalog/services/app-configuration). Eg:-
  `us-south`, `au-syd` etc.
- guid : GUID of the App Configuration service. Get it from the service instance credentials section of the dashboard
- apikey : ApiKey of the App Configuration service. Get it from the service instance credentials section of the dashboard
- collectionId : Id of the collection created in App Configuration service instance under the **Collections** section.
- environmentId : Id of the environment created in App Configuration service instance under the **Environments** section.

## Get single feature

```kt
val feature: Feature? = appConfigClient.getFeature("online-check-in")
if (feature != null) {
    println("Feature Name : ${feature.getFeatureName()}")
    println("Feature Id : ${feature.getFeatureId()}")
    println("Feature Type : ${feature.getFeatureDataType()}")
    println("Is feature enabled? : ${feature.isEnabled()}")
}
```

## Get all features

```kt
val features: HashMap<String, Feature>? = appConfigClient.getFeatures()
```

## Evaluate a feature

Use the `feature.getCurrentValue(entityId, entityAttributes)` method to evaluate the value of the
feature flag. This method returns one of the Enabled/Disabled/Overridden value based on the
evaluation.

```kt
val entityId = "john_doe"
val entityAttributes = JSONObject()

try {
    entityAttributes.put("city", "Bangalore")
    entityAttributes.put("country", "India")
} catch (e: JSONException) {
    e.printStackTrace()
}

val appConfigClient = AppConfiguration.getInstance()
val feature: Feature? = appConfigClient.getFeature("online-check-in")
if (feature != null) {
    val value = feature.getCurrentValue(entityId, entityAttributes)
}
```

* entityId: Id of the Entity. This will be a string identifier related to the Entity against which
  the feature is evaluated. For example, an entity might be an instance of an app that runs on a
  mobile device, a microservice that runs on the cloud, or a component of infrastructure that runs
  that microservice. For any entity to interact with App Configuration, it must provide a unique
  entity ID.
* entityAttributes: A JSON object consisting of the attribute name and their values that defines the
  specified entity. This is an optional parameter if the feature flag is not configured with any
  targeting definition. If the targeting is configured, then entityAttributes should be provided for
  the rule evaluation. An attribute is a parameter that is used to define a segment. The SDK uses
  the attribute values to determine if the specified entity satisfies the targeting rules, and
  returns the appropriate feature flag value.

## Get single Property

```kt
val property: Property? = appConfigClient.getProperty("check-in-charges")
if (property != null) {
    println("Property Name : ${property.getPropertyName()}")
    println("Property Id : ${property.getPropertyId()}")
    println("Property Type : ${property.getPropertyDataType()}")
}
```

## Get all Properties

```kt
val properties: HashMap<String, Property>? = appConfigClient.getProperties()
```

## Evaluate a property

Use the `property.getCurrentValue(entityId, entityAttributes)` method to evaluate the value of the
property. This method returns the default property value or its overridden value based on the
evaluation.

```kt
val entityId = "john_doe"
val entityAttributes = JSONObject()

try {
    entityAttributes.put("city", "Bangalore")
    entityAttributes.put("country", "India")
} catch (e: JSONException) {
    e.printStackTrace()
}

val appConfigClient = AppConfiguration.getInstance()
val property: Property? = appConfigClient.getProperty("check-in-charges")
if (property != null) {
    val value = property.getCurrentValue(entityId, entityAttributes)
}
```

* entityId: Id of the Entity. This will be a string identifier related to the Entity against which
  the property is evaluated. For example, an entity might be an instance of an app that runs on a
  mobile device, a microservice that runs on the cloud, or a component of infrastructure that runs
  that microservice. For any entity to interact with App Configuration, it must provide a unique
  entity ID.
* entityAttributes: A JSON object consisting of the attribute name and their values that defines the
  specified entity. This is an optional parameter if the property is not configured with any
  targeting definition. If the targeting is configured, then entityAttributes should be provided for
  the rule evaluation. An attribute is a parameter that is used to define a segment. The SDK uses
  the attribute values to determine if the specified entity satisfies the targeting rules, and
  returns the appropriate property value.

## Fetching the appConfigClient across other modules

Once the SDK is initialized, the appConfigClient can be obtained across other modules as shown
below:

```kt
// **other modules**

import com.ibm.cloud.appconfiguration.android.sdk.AppConfiguration
val appConfigClient = AppConfiguration.getInstance()

val feature: Feature? = appConfigClient.getFeature("online-check-in")
val enabled = feature.isEnabled()
val featureValue = feature.getCurrentValue(entityId, entityAttributes)
```

## Supported Data types

App Configuration service allows to configure the feature flag and properties in the following data types : Boolean,
Numeric, String. The String data type can be of the format of a text string , JSON or YAML. The SDK processes each
format accordingly as shown in the below table.

<details><summary>View Table</summary>

| **Feature or Property value**                                                                          | **DataType** | **DataFormat** | **Type of data returned <br> by `getCurrentValue()`** | **Example output**                                                   |
| ------------------------------------------------------------------------------------------------------ | ------------ | -------------- | ----------------------------------------------------- | -------------------------------------------------------------------- |
| `true`                                                                                                 | BOOLEAN      | not applicable | `java.lang.Boolean`                                                | `true`                                                               |
| `25`                                                                                                   | NUMERIC      | not applicable | `java.lang.Integer`                                             | `25`                                                                 |
| "a string text"                                                                                        | STRING       | TEXT           | `java.lang.String`                                              | `a string text`                                                      |
| <pre>{<br>  "firefox": {<br>    "name": "Firefox",<br>    "pref_url": "about:config"<br>  }<br>}</pre> | STRING       | JSON           | `org.json.JSONObject`                              | `{"firefox":{"name":"Firefox","pref_url":"about:config"}}` |
| <pre>men:<br>  - John Smith<br>  - Bill Jones<br>women:<br>  - Mary Smith<br>  - Susan Williams</pre>  | STRING       | YAML           | `java.lang.String`                              | `"men:\n  - John Smith\n  - Bill Jones\nwomen:\n  - Mary Smith\n  - Susan Williams"` |
</details>

<details><summary>Feature flag</summary>

  ```kt
  val feature: Feature? = appConfigClient.getFeature("json-feature")
  feature.getFeatureDataType() // STRING
  feature.getFeatureDataFormat() // JSON

  // Example below (traversing the returned JSONObject)
  if (feature != null) {
    val result = feature.getCurrentValue(entityId, entityAttributes) as JSONObject
    result.get("key") // returns the value of the key
  }
  
  val feature: Feature? = appConfigClient.getFeature("yaml-feature")
  feature.getFeatureDataType() // STRING
  feature.getFeatureDataFormat() // YAML
  feature.getCurrentValue(entityId, entityAttributes) // returns the stringified yaml (check above table)
  ```

</details>
<details><summary>Property</summary>

  ```javascript
  val property: Property? = appConfigClient.getProperty("json-property")
  property.getPropertyDataType() // STRING
  property.getPropertyDataFormat() // JSON

  // Example below (traversing the returned JSONObject)
  if (property != null) {
    val result = property.getCurrentValue(entityId, entityAttributes) as JSONObject
    result.get("key") // returns the value of the key
  }

  val property: Property? = appConfigClient.getProperty("yaml-property")
  property.getPropertyDataType() // STRING
  property.getPropertyDataFormat() // YAML
  property.getCurrentValue(entityId, entityAttributes) // returns the stringified yaml (check above table)
  ```
</details>

## Set listener for the feature and property data changes

The SDK provides mechanism to notify you in real-time when feature flag's or property's configuration
changes. You can subscribe to configuration changes using the same appConfigClient.

```kt
import com.ibm.cloud.appconfiguration.android.sdk.configurations.ConfigurationUpdateListener

appConfigClient.registerConfigurationUpdateListener(object : ConfigurationUpdateListener {
    override fun onConfigurationUpdate() {
        println("Received updates on configurations")
        // **add your code**
        // To find the effect of any configuration changes, you can call the feature or property related methods
        
        // val feature: Feature? = appConfigClient.getFeature("online-check-in")
        // if (feature != null) {
        //    val newValue = feature.getCurrentValue(entityId, entityAttributes)
        // }
    }
})

```

## Enable/Disable Logger (optional)

Use this method to enable/disable the logging in SDK.

```kt
val appConfigClient = AppConfiguration.getInstance()

// Enable Logger 
appConfigClient.enableDebug(true)

// Disable Logger
appConfigClient.enableDebug(false)

```

## Force fetch the configurations from server

Fetch the latest configuration data. 

```kt
appConfigClient.fetchConfigurations()
```


# Java

## Installation

Choose to integrate the AppConfiguration Android client SDK package using either of the following options:

- Download and import the package to your Android Studio project
- Get the package through Gradle

## Import SDK

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
	        implementation "com.ibm.cloud:appconfiguration-android-sdk:0.3.4"
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
## Initialize AppConfiguration SDK

```java
import com.ibm.cloud.appconfiguration.android.sdk.AppConfiguration;

String collectionId = "airlines-webapp";
String environmentId = "dev";
    
AppConfiguration appConfigClient = AppConfiguration.getInstance();
appConfigClient.init(getApplication(), 
                    "region", "guid", "apikey");
appConfigClient.setContext(collectionId, environmentId);
```
:red_circle: **Important** :red_circle:

The **`init()`** and **`setContext()`** are the initialisation methods and should be invoked **only
once** using appConfigClient. The appConfigClient, once initialised, can be obtained across modules
using **`AppConfiguration.getInstance()`**.  [See this example below](#fetching-the-appconfigclient-across-other-modules-1).

- region : Region name where the App Configuration service instance is created.
  See list of supported locations [here](https://cloud.ibm.com/catalog/services/app-configuration).
  Eg:- `us-south`, `au-syd` etc.
- guid : GUID of the App Configuration service. Get it from the service instance credentials section of the dashboard
- apikey : ApiKey of the App Configuration service. Get it from the service instance credentials section of the dashboard
- collectionId : Id of the collection created in App Configuration service instance under the **Collections** section.
- environmentId : Id of the environment created in App Configuration service instance under the **Environments** section.

## Get single feature

```java
Feature feature = appConfigClient.getFeature("online-check-in"); // feature can be null incase of an invalid feature id

if (feature != null) {
    System.out.println("Feature Name : " + feature.getFeatureName());
    System.out.println("Feature Id : " + feature.getFeatureId());
    System.out.println("Feature Type : " + feature.getFeatureDataType());
    System.out.println("Is feature enabled? : " + feature.isEnabled());
}
```

## Get all feature

```java
HashMap<String,Feature> features =  appConfigClient.getFeatures();

```

## Evaluate a feature

Use the `feature.getCurrentValue(entityId, entityAttributes)` method to evaluate the value of the
feature flag. This method returns one of the Enabled/Disabled/Overridden value based on the
evaluation.

```java
String entityId = "john_doe";
JSONObject entityAttributes = new JSONObject();

try {
    entityAttributes.put("city", "Bengaluru");
    entityAttributes.put("country", "India");
} catch (JSONException e) {
    e.printStackTrace();
}

AppConfiguration appConfigClient = AppConfiguration.getInstance();
Feature feature = appConfigClient.getFeature("online-check-in");
if (feature != null) {
   String value = (String) feature.getCurrentValue(entityId, entityAttributes);
}
```

* entityId: Id of the Entity. This will be a string identifier related to the Entity against which
  the feature is evaluated. For example, an entity might be an instance of an app that runs on a
  mobile device, a microservice that runs on the cloud, or a component of infrastructure that runs
  that microservice. For any entity to interact with App Configuration, it must provide a unique
  entity ID.
* entityAttributes: A JSON object consisting of the attribute name and their values that defines the
  specified entity. This is an optional parameter if the feature flag is not configured with any
  targeting definition. If the targeting is configured, then entityAttributes should be provided for
  the rule evaluation. An attribute is a parameter that is used to define a segment. The SDK uses
  the attribute values to determine if the specified entity satisfies the targeting rules, and
  returns the appropriate feature flag value.

## Get single Property

```java
Property property = appConfigClient.getProperty("check-in-charges"); // property can be null incase of an invalid property id

if (property != null) {
    System.out.println("Property Name : " + property.getPropertyName());
    System.out.println("Property Id : " + property.getPropertyId());
    System.out.println("Property Type : " + property.getPropertyDataType());
}
        
```

## Get all Properties

```java
HashMap<String,Property> properties =  appConfigClient.getProperties();
```

## Evaluate a property

Use the `property.getCurrentValue(entityId, entityAttributes)` method to evaluate the value of the
property. This method returns the default property value or its overridden value based on the
evaluation.

```java

String entityId = "john_doe";
JSONObject entityAttributes = new JSONObject();

try {
    entityAttributes.put("city", "Bengaluru");
    entityAttributes.put("country", "India");
} catch (JSONException e) {
    e.printStackTrace();
}

AppConfiguration appConfigClient = AppConfiguration.getInstance();
Property property = appConfigClient.getProperty("check-in-charges");

if (property != null) {
    String value = (String) property.getCurrentValue(entityId, entityAttributes);
}
```

* entityId: Id of the Entity. This will be a string identifier related to the Entity against which
  the property is evaluated. For example, an entity might be an instance of an app that runs on a
  mobile device, a microservice that runs on the cloud, or a component of infrastructure that runs
  that microservice. For any entity to interact with App Configuration, it must provide a unique
  entity ID.
* entityAttributes: A JSON object consisting of the attribute name and their values that defines the
  specified entity. This is an optional parameter if the property is not configured with any
  targeting definition. If the targeting is configured, then entityAttributes should be provided for
  the rule evaluation. An attribute is a parameter that is used to define a segment. The SDK uses
  the attribute values to determine if the specified entity satisfies the targeting rules, and
  returns the appropriate property value.

## Fetching the appConfigClient across other modules

Once the SDK is initialized, the appConfigClient can be obtained across other modules as shown
below:

```java
// **other modules**

import com.ibm.cloud.appconfiguration.sdk.AppConfiguration;
AppConfiguration appConfigClient = AppConfiguration.getInstance();

Feature feature = appConfigClient.getFeature("string-feature");
boolean enabled = feature.isEnabled();
String featureValue = (String) feature.getCurrentValue(entityId, entityAttributes);
```

## Supported Data types

App Configuration service allows to configure the feature flag and properties in the following data types : Boolean,
Numeric, String. The String data type can be of the format of a text string , JSON or YAML. The SDK processes each
format accordingly as shown in the below table.

<details><summary>View Table</summary>

| **Feature or Property value**                                                                          | **DataType** | **DataFormat** | **Type of data returned <br> by `getCurrentValue()`** | **Example output**                                                   |
| ------------------------------------------------------------------------------------------------------ | ------------ | -------------- | ----------------------------------------------------- | -------------------------------------------------------------------- |
| `true`                                                                                                 | BOOLEAN      | not applicable | `java.lang.Boolean`                                                | `true`                                                               |
| `25`                                                                                                   | NUMERIC      | not applicable | `java.lang.Integer`                                             | `25`                                                                 |
| "a string text"                                                                                        | STRING       | TEXT           | `java.lang.String`                                              | `a string text`                                                      |
| <pre>{<br>  "firefox": {<br>    "name": "Firefox",<br>    "pref_url": "about:config"<br>  }<br>}</pre> | STRING       | JSON           | `org.json.JSONObject`                              | `{"firefox":{"name":"Firefox","pref_url":"about:config"}}` |
| <pre>men:<br>  - John Smith<br>  - Bill Jones<br>women:<br>  - Mary Smith<br>  - Susan Williams</pre>  | STRING       | YAML           | `java.lang.String`                              | `"men:\n  - John Smith\n  - Bill Jones\nwomen:\n  - Mary Smith\n  - Susan Williams"` |
</details>

<details><summary>Feature flag</summary>

  ```java
  Feature feature = appConfigClient.getFeature("json-feature");
  feature.getFeatureDataType(); // STRING
  feature.getFeatureDataFormat(); // JSON

  // Example below (traversing the returned JSONObject)
  if (feature != null) {
    JSONObject result = (JSONObject) feature.getCurrentValue(entityId, entityAttributes);
    result.get("key") // returns the value of the key
  }
  
  Feature feature = appConfigClient.getFeature("yaml-feature");
  feature.getFeatureDataType(); // STRING
  feature.getFeatureDataFormat(); // YAML
  feature.getCurrentValue(entityId, entityAttributes); // returns the stringified yaml (check above table)
  ```

</details>
<details><summary>Property</summary>

  ```java
  Property property = appConfigClient.getProperty("json-property");
  property.getPropertyDataType(); // STRING
  property.getPropertyDataFormat(); // JSON

  // Example below (traversing the returned JSONObject)
  if (property != null) {
    JSONObject result = (JSONObject) property.getCurrentValue(entityId, entityAttributes);
    result.get("key") // returns the value of the key
  }
  
  Property property = appConfigClient.getProperty("yaml-property");
  property.getPropertyDataType(); // STRING
  property.getPropertyDataFormat(); // YAML
  property.getCurrentValue(entityId, entityAttributes); // returns the stringified yaml (check above table)
  ```
</details>

## Listen to the feature changes.

The SDK provides mechanism to notify you in real-time when feature flag's or property's configuration
changes. You can subscribe to configuration changes using the same appConfigClient.

```java
import com.ibm.cloud.appconfiguration.android.sdk.configurations.ConfigurationUpdateListener;

appConfigClient.registerConfigurationUpdateListener(new ConfigurationUpdateListener() {
    @Override
    public void onConfigurationUpdate() {
        System.out.println("Received update on configurations");
        // **add your code**
        // To find the effect of any configuration changes, you can call the feature or property related methods
        //
        // Feature feature = appConfigClient.getFeature("numeric-feature");
        // if (feature != null) {
        //      Integer newValue = (Integer) feature.getCurrentValue(entityId, entityAttributes);
        // }
    }
});
```

## Enable debugger (Optional)

Use this method to enable/disable the logging in SDK.

```Java
AppConfiguration appConfigClient = AppConfiguration.getInstance();

// Enable Logger 
appConfigClient.enableDebug(true);

// Disable Logger
appConfigClient.enableDebug(false);

```

## Force fetch the configurations from server

Fetch the latest configuration data. 

```Java
appConfigClient.fetchConfigurations();
```

## Examples 

The [examples](https://github.com/IBM/appconfiguration-android-client-sdk/tree/master/app) folder has the examples. 

## License

This project is released under the Apache 2.0 license. The license's full text can be found in [LICENSE](https://github.com/IBM/appconfiguration-android-client-sdk/blob/master/LICENSE)
