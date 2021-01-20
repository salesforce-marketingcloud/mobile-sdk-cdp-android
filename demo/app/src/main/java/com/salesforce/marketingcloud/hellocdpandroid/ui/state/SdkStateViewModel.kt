package com.salesforce.marketingcloud.hellocdpandroid.ui.state

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.salesforce.marketingcloud.cdp.CdpSdk
import com.salesforce.marketingcloud.cdp.consent.Consent
import com.salesforce.marketingcloud.cdp.events.Event
import com.salesforce.marketingcloud.cdp.location.Coordinates

class SdkStateViewModel : ViewModel() {

    private val _message = MutableLiveData<String>().apply {
        value = ""
    }
    val message: LiveData<String> = _message

    private val _text = MutableLiveData<String>().apply {
        CdpSdk.requestSdk {
            value = it.state.toString(4)
        }
    }
    val text: LiveData<String> = _text

    private val _consent = MutableLiveData<Boolean>().apply {
        CdpSdk.requestSdk {
            value = it.consent == Consent.OPT_IN
        }
    }
    val consent: LiveData<Boolean> = _consent

    fun clearMessage() {
        _message.value = ""
    }

    fun toggleConsent(granted: Boolean) {
        CdpSdk.requestSdk {
            // Set Consent
            it.consent = if (granted) Consent.OPT_IN else Consent.OPT_OUT

            // Update View, Message User
            _consent.value = it.consent == Consent.OPT_IN
            _message.value = "Consent Toggled: ${it.consent}"
            refreshSdkState()
        }
    }

    fun setLocation() {
        CdpSdk.requestSdk {
            // Set Location Coordinates and Expiration
            val coordinates = Coordinates(0.00, -0.00)
            it.setLocation(coordinates, 300 /* 5 min */)

            // Update View, Message User
            _message.value = "Location Set: $coordinates"
            refreshSdkState()
        }
    }

    fun trackEngagementEvent() {
        val event = Event.engagement("UserInteraction", mapOf("ButtonClicked" to "EngagementEvent"))
        trackEvent(event)
    }

    fun trackProfileEvent(name: String) {
        val event = Event.profile("UpdateUser", mapOf("UserName" to name))
        trackEvent(event)
    }

    private fun trackEvent(event: Event?) = CdpSdk.requestSdk {
        // Track Event
        it.track(event)

        // Update View, Message User
        _message.value = "Event Tracked: $event"
        refreshSdkState()
    }

    // Update View
    private fun refreshSdkState() {
        CdpSdk.requestSdk {
            _text.value = it.state.toString(4)
            Log.d("SdkStateViewModel", it.state.toString(4))
        }
    }
}