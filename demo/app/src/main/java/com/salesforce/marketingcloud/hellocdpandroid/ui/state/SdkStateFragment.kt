package com.salesforce.marketingcloud.hellocdpandroid.ui.state

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.salesforce.marketingcloud.hellocdpandroid.R

class SdkStateFragment : Fragment() {

    private lateinit var sdkStateViewModel: SdkStateViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sdkStateViewModel =
            ViewModelProvider(this).get(SdkStateViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_sdk_state, container, false)

        // Show SDK State
        val textView: TextView = root.findViewById(R.id.text_sdk_state)
        sdkStateViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        // Handle Consent
        val consentSwitch: SwitchCompat = root.findViewById(R.id.switch_consent)
        sdkStateViewModel.consent.observe(viewLifecycleOwner, {
            consentSwitch.isChecked = it
        })
        consentSwitch.setOnClickListener {
            sdkStateViewModel.toggleConsent((it as SwitchCompat).isChecked)
        }

        // Set Location
        val locationButton: Button = root.findViewById(R.id.btn_location)
        locationButton.setOnClickListener {
            sdkStateViewModel.setLocation()
        }

        // Track Engagement Event
        val engagementButton: Button = root.findViewById(R.id.btn_engagement)
        engagementButton.setOnClickListener {
            sdkStateViewModel.trackEngagementEvent()
        }

        // Track Profile Event
        val profileButton: Button = root.findViewById(R.id.btn_profile)
        profileButton.setOnClickListener {
            sdkStateViewModel.trackProfileEvent("John Doe")
        }

        sdkStateViewModel.message.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                Snackbar.make(root, it, 3000).show()
                sdkStateViewModel.clearMessage()
            }
        })
        return root
    }
}