package com.salesforce.marketingcloud.hellocdpandroid

import android.app.Application
import android.content.Context
import android.util.Log
//import com.salesforce.marketingcloud.MarketingCloudConfig
import com.salesforce.marketingcloud.cdp.CdpConfig
import com.salesforce.marketingcloud.cdp.InitializationStatus
//import com.salesforce.marketingcloud.notifications.NotificationCustomizationOptions
import com.salesforce.marketingcloud.sfmcsdk.SFMCSdk
import com.salesforce.marketingcloud.sfmcsdk.SFMCSdkModuleConfig
import com.salesforce.marketingcloud.sfmcsdk.components.logging.LogLevel
import com.salesforce.marketingcloud.sfmcsdk.components.logging.LogListener
import java.util.*

class HelloCdpApp : Application() {

    companion object {
        val logOutput = mutableListOf<LogRow>()
    }

    /**
     * Connect Mobile Apps to Data Cloud
     * https://help.salesforce.com/s/articleView?id=sf.c360_a_set_up_mobile_web_connection.htm&type=5
     *
     * Sample Mobile Schema:
     * https://cdn.c360a.salesforce.com/cdp/schemas/238/mobile-connector-schema.json
     */
    override fun onCreate() {
        super.onCreate()

        // Logging and Debugging
        // https://developer.salesforce.com/docs/atlas.en-us.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk_logging_and_debugging.htm
        if (BuildConfig.DEBUG) {
            SFMCSdk.setLogging(LogLevel.DEBUG, getLogListener())
        }

        // Initialize SDK
        SFMCSdk.configure(this, SFMCSdkModuleConfig.build {
            this.cdpModuleConfig = getCdpModuleConfig(this@HelloCdpApp)
//            this.pushModuleConfig = getPushModuleConfig(this@HelloCdpApp)
        }) {
            if (it.status == InitializationStatus.SUCCESS) {
                Log.d("~#HelloCdpApp", "SDK modules are initialized and ready.")
            }
        }

        // Requests to the SDK to be executed after initialization is complete.
        SFMCSdk.requestSdk { sdk ->
            /*
            The SDK already collects advertiserId to be sent after CDP initialization as an "identity" event,
            along with: registrationId/softwareApplicationId, softwareApplicationName, softwareApplicationVersion
            osName, osVersion, deviceType

            Event.getAdvertisingId(this) { advertiserId ->
                sdk.identity.setProfileAttribute("advertiserId", advertiserId, ModuleIdentifier.CDP)
            }
             */
        }
    }

    /**
     * Customer Data Platform Developer Guide
     * https://developer.salesforce.com/docs/atlas.en-us.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk.htm
     *
     * app/build.gradle dependency:
     * implementation 'com.salesforce.marketingcloud:cdp:2.0.+'
     */
    private fun getCdpModuleConfig(context: Context): CdpConfig {

        // TODO: Replace CDP (Customer Data Platform) properties
        val appId = "{appIdFromMobileConnector}"
        val endpoint = "https://{endpointFromMobileConnector}"

        /**
         * Capturing Behavior Data (Screen & Lifecycle Events)
         * https://developer.salesforce.com/docs/atlas.en-us.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk_capturing_behavior_data.htm
         *
         * Behavior Schema
         * https://developer.salesforce.com/docs/atlas.en-us.236.0.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk_behavior_schema.htm
         */
        return CdpConfig.Builder(context, appId, endpoint)
            .trackScreens(true) // default false
            .trackLifecycle(true) // default false
            .sessionTimeout(600) // default 600
            .build()
    }

    /**
     * MobilePush SDK
     * https://salesforce-marketingcloud.github.io/MarketingCloudSDK-Android/
     *
     * build.gradle dependency:
     * implementation 'com.salesforce.marketingcloud:marketingcloudsdk:8.0.+'
     */
//    private fun getPushModuleConfig(context: Context): MarketingCloudConfig {
//        return MarketingCloudConfig.builder().apply {
//
//            // TODO: Replace Marketing Cloud properties
//            setApplicationId("{mc_application_id}")
//            setAccessToken("{mc_access_token}")
//            setSenderId("{fcm_sender_id}")
//            setMarketingCloudServerUrl("{marketing_cloud_url}")
//            setMid("{mid}")
//
//            setNotificationCustomizationOptions(NotificationCustomizationOptions.create(R.drawable.ic_notifications_black_24dp))
//        }.build(context)
//    }

    private fun getLogListener(): LogListener {
        return object : LogListener {
            override fun out(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
                Log.d(tag, message, throwable)
                logOutput.add(LogRow(level, tag, message, throwable))
            }
        }
    }

    data class LogRow(
        val level: LogLevel,
        val tag: String,
        val message: String,
        val throwable: Throwable?
    ) {
        private val timestamp: String = Date().toString()

        override fun toString(): String {
            return if (throwable != null) "$timestamp $level $tag $message $throwable\n\n" else "$timestamp $level $tag $message\n\n"
        }
    }
}
