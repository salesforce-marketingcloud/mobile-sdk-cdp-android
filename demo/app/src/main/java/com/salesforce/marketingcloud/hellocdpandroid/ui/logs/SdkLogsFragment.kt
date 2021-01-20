package com.salesforce.marketingcloud.hellocdpandroid.ui.logs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.salesforce.marketingcloud.hellocdpandroid.R

class SdkLogsFragment : Fragment() {

    private lateinit var sdkLogsViewModel: SdkLogsViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        sdkLogsViewModel =
                ViewModelProvider(this).get(SdkLogsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_sdk_logs, container, false)
        val textView: TextView = root.findViewById(R.id.text_dashboard)
        sdkLogsViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}