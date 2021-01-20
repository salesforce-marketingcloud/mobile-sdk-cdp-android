package com.salesforce.marketingcloud.hellocdpandroid

import android.app.Application
import android.util.Log
import com.salesforce.marketingcloud.cdp.CdpConfig
import com.salesforce.marketingcloud.cdp.CdpSdk
import com.salesforce.marketingcloud.core.components.logging.LogLevel
import com.salesforce.marketingcloud.core.components.logging.LogListener
import java.util.*

class HelloCdpApp : Application() {

    companion object {
        val logOutput = mutableListOf<LogRow>()
    }

    override fun onCreate() {
        super.onCreate()

        // Enable Logging
        CdpSdk.logLevel(LogLevel.DEBUG, object : LogListener {
            override fun out(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
                Log.d(tag, message, throwable)
                logOutput.add(LogRow(level, tag, message, throwable))
            }
        })

        // Create Configuration
        val config =
            CdpConfig.Builder(this, APPLICATION_ID, URL_END_POINT)
                .trackScreens(true|false)
                .trackLifecyle(true|false)
                .sessionTimeout(SESSION_TIMEOUT_IN_SECONDS)
                .build()

        // Initialize SDK
        CdpSdk.configure(config)
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