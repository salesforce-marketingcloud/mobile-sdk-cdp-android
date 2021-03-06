# Salesforce MarketingCloud CDP SDK - Android

The Salesforce CDP SDK makes it possible for brands to integrate their mobile applications with [Salesforce Customer 360 Audiences](https://www.salesforce.com/products/marketing-cloud/customer-data-platform/) to capture rich behavioral and demographic data from end users, including app lifecycle and screen navigation events.

## Prerequisites
Integrating your mobile application with this SDK requires a Customer 360 Audience Mobile App Source ID and Tenant Specific Endpoint. See [C360 Audiences Integration Guide](https://help.salesforce.com/articleView?id=c360_a_web_mobile_app_connector.htm&type=5).

## KDoc / Javadoc
See [KDoc](https://salesforce-marketingcloud.github.io/mobile-sdk-cdp-android/kdoc/1.0.0/index.html) for complete SDK code definitions.

## Installation

### Update module-level `build.gradle` file
Add the version-safety-plugin classpath and repository
```groovy
buildscript {
  repositories {
    maven { url "https://salesforce-marketingcloud.github.io/MarketingCloudSDK-Android/repository" }
  }
  dependencies {
    classpath 'com.salesforce.marketingcloud:version-safety-plugin:latest.release'
  }
}
```

Add the required SDK repositories:
```groovy
allprojects {
  repositories {
    maven { url "https://salesforce-marketingcloud.github.io/MarketingCloudSDK-Android/repository" }
    maven { url "https://salesforce-marketingcloud.github.io/mobile-sdk-cdp-android/repository" }
  }
}
```
### Update app-level `build.gradle` file
Apply the version-safety-plugin
```groovy
apply plugin: 'com.salesforce.marketingcloud.version-safety-plugin'
```

Add the SDK dependency:
```groovy
dependencies {
  implementation 'com.salesforce.marketingcloud:cdp:1.0.0'
}
```

## Quickstart Code Sample
```kotlin
// Enable Logging (Optional)
CdpSdk.logLevel(LogLevel.DEBUG, LogListener.AndroidLogger())

// Create configuration
val config = CdpConfig.Builder(context, appId, endpoint).build()

// Initialize SDK
CdpSdk.configure(config)

// Request SDK Instance
CdpSdk.requestSdk { sdk ->
  // Set SDK Consent state
  sdk.consent = Consent.OPT_IN

  // Set SDK Location for 1 minute
  sdk.setLocation(coordinates: Coordinates(90.0, -90.0), expiresIn: 60)

  // Create Engagement Event
  val engagementEvent = Event.engagement(
    eventType = "AddToCart", 
    eventAttributes = mapOf("sku" to "ABC-123", "price" to 19.99)
  )

  // Track the Engagement Event
  sdk.track(event: engagementEvent)

  // Create Profile Event
  val profileEvent = Event.profile(
    eventType = "Identify", 
    eventAttributes = mapOf("firstName" to "John", "lastName" to "Doe")
  )

  // Track the Profile Event
  sdk.track(event: profileEvent)

  // Debugging
  val cdpState = sdk.state.toString(4)
  Log.d("CDP SDK State", cdpState)
}
```
## Detailed Implementation

### <a name="configure"></a> Configure the SDK
To obtain an initialized instance of the SDK object, you must first provide a required configuration. 

1. Build the CDP SDK configuration, providing the application id and Tenant Specific Endpoint (TSE).
    ```kotlin
    val config = CdpConfig.Builder(context, appId, endpoint)
        .trackScreens(<true/false>) // optional, default false
        .trackLifecycle(<true/false>) // optional, default false
        .sessionTimeout(<seconds>) // optional, default 600
        .build()
    ```
2. Call the configure method on the CDP SDK, passing the config.
    ```kotlin
    CdpSdk.configure(config)
    ```
Congratulations! The CDP SDK is now configured and ready to use.

| Config Option | Description |
| :---          | :--- |
| trackScreens | If enabled, "ScreenView" events will automatically be collected. |
| trackLifecycle | If enabled, lifecycle events (AppUpdate, AppFirstLaunch, AppLaunch) will automatically be collected. |
| sessionTimeout | Time (in seconds) an app can stay in the background before the session expires. Default is 600 seconds (10 minutes). |

### Accessing the CDP SDK
The CDP SDK initializes asynchronously on a background thread. We provide a method + callback that returns the SDK as soon as it is available. If the SDK has already initialized, the listener will immediately return the instance.
```kotlin
CdpSdk.requestSdk { sdk ->
  // sdk = initialized CDP SDK
}
```

### Granting Consent
The mobile application will obtain consent to track events from the end user and set the SDK's consent property accordingly, as shown below. Tracked events will be transmitted to the Salesforce CDP service only when the SDK consent property is set to OPT_IN.

```kotlin
CdpSdk.requestSdk { sdk ->
  
  // Consent state is retained once set. It does not need to be called with every initialization.

  // Sets SDK consent to OPT_IN
  sdk.consent = Consent.OPT_IN

  // Sets SDK consent to OPT_OUT
  sdk.consent = Consent.OPT_OUT
}
```

| Consent State | Description |
| :---          | :--- |
| PENDING       | Default state <br> Tracked events will not be transmitted off device <br> Lifecycle and Screen Tracking Events will be queued locally <br> Engagement and Profile events will be discarded |
| OPT_IN        | All events will be transmitted |
| OPT_OUT       | All events will be discarded |

<em>Once consent has been set to OPT_IN or OPT_OUT, consent cannot be reset to PENDING.</em>

### Tracking Events
The CDP SDK enables collection of granular data about end users’ mobile app interactions through event tracking. An event consists of event type, and event attributes (optional). The CDP SDK currently supports two event categories, engagement and profile.

```kotlin
CdpSdk.requestSdk { sdk ->
  sdk.consent = Consent.OPT_IN

  // Create Engagement Event
  val engagementEvent = Event.engagement(
    eventType = "AddToCart", 
    eventAttributes = mapOf("sku" to "ABC-123", "price" to 19.99)
  )

  // Track the Engagement Event
  sdk.track(event: engagementEvent)

  // Grab Android Advertising Id
  Event.getAdvertisingId(context: this) { advertisingId -> 
  
    // Create Profile Event
    val profileEvent = Event.profile(
      eventType = "Identify", 
      eventAttributes = mapOf("firstName" to "John", "lastName" to "Doe", "maid": advertisingId)
    )

    // Track the Profile Event
    sdk.track(event: profileEvent)
  }
}
```
#### Note
* For best performance and battery-life, the CDP SDK queues events locally and transmits to Salesforce CDP in batches. The SDK will send events upon the app entering the foreground, moving to the background, or when the queue size hits 20.
* If an invalid eventType is used, the Event constructor will return null.
  * Reserved event types (case-insensitive): "AppFirstLaunch", "AppLaunch", "AppUpdate", "ScreenView"
  * Invalid event type: empty/blank String
* Invalid attributes will be dropped from the event, while retaining valid key/value pairs.
  * Reserved attribute keys (case-insensitive): "deviceId", "userId", "eventId", "sessionId", "dateTime", "eventType", "Category", "Latitude", "Longitude"
  * Allowed attribute value types: String, Boolean, Int, Long, Double, Float, Byte, Short, Char, null

### Location Tracking
Location tracking is supported for all events. Enable this functionality by using the code shown below.

```kotlin
CdpSdk.requestSdk { sdk ->
  // Prepare the coordinates
  val coordinates = Coordinates(latitude: 54.187738, longitude: 15.554440)

  // Set the location coordinates to be valid for 60 seconds
  sdk.setLocation(coordinates: coordinates, expiresIn: 60)

  // Manually clear location before expiration
  sdk.setLocation(null, 0)
}
```
When a location is provided to the SDK, all tracked events will have "latitude" and "longitude" attributes attached. When the location has expired, or is cleared, "latitude" and "longitude" will no longer be appended to new events. The chart below visualizes how locations are attached.

![Location Tracking Diagram](/assets/diagram_location_tracking.jpeg)


### Debugging

#### Logging
Logging is an optional, yet critical, SDK feature that allows the mobile application developer to select the verbosity of the CDP SDK’s output. By default, logging is disabled. Logging can be enabled as shown below.
```kotlin
// Logging should be set before the SDK is configured to ensure all logs are captured
CdpSdk.logLevel(logLevel: LogLevel.DEBUG, logListener: LogListener.AndroidLogger())
```

<em>The logListener parameter accepts an interface of type LogListener. We provide a standard implementation `LogListener.AndroidLogger` that immediately outputs logs to Android's Logcat. A custom listener can be provided for more control over SDK logs.</em>

| Log Level Types | Description |
| :---          | :--- |
| DEBUG | This log level provides granular, low-level information about how the SDK processes tasks, events and error details. Debug logging enables developers to perform diagnostics on their application to troubleshoot issues. |
| WARNING | This log level indicates the SDK integration might have a problem or it encountered an unusual situation. Warnings are associated with potentially harmful, but recoverable errors. |
| ERROR | This log level provides details about unrecoverable errors usually associated with inability to complete valuable business use cases. |
| NONE | Default, no logs will be output |

#### SDK State
The state property on the CDP SDK shared instance returns a JSONObject containing current configuration settings, session details, event queue size, and consent information. This information is critical for debugging and troubleshooting purposes.

```kotlin
CdpSdk.requestSdk { sdk ->
  // Get the CDP SDK state
  val state = sdk.state

  // Convert state to string and log to Logcat
  Log.d("CDP SDK State", sdk.state.toString(4))
}
```

The SDK state will output similar to the block below.
```json
{
    "name":"cdp",
    "config": {
        "appId":"YOUR APP ID",
        "endpoint":"YOUR ENDPOINT",
        "sessionDuration":600,
        "trackLifecycle":false,
        "trackScreens":false
    },
    "moduleState": "READY",
    "consentManager": {
        "deviceId":"YOUR DEVICE ID",
        "consent":"opt_in"
    },
    "sessionManager": {
        "sessionId":"SESSION ID"
    },
    "eventManager": {
        "queueSize":2
    },
    "locationManager": {
      "latitude": 54.187738,
      "longitude": 15.554440,
      "expiration": 1610390993563
    }
}
```

### Troubleshooting

**I cannot set Consent to "PENDING".** <br>
A: Consent cannot be set to “pending”. Pending state of consent indicates that CDP SDK was initialized with its system default value and was not yet deliberately set. You can set consent to either "OPT_IN" or "OPT_OUT" value.

**My events got purged from the queue before sending.** <br>
A: Events are discarded from the queue only when consent is set to opt_out. You can verify consent status by inspecting the SDK state.

**My events are not being collected. My queue size doesn’t reflect tracked events.** <br>
A: Please make sure that the consent is set to opt_in. No events are collected when consent is set to opt_out, and only Lifecycle and Screen tracking events are collected when consent is pending.

### Support
When contacting support, please provide the SDK state and SDK logs in DEBUG mode.
