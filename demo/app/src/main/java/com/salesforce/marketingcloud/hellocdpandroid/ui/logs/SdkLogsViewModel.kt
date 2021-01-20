package com.salesforce.marketingcloud.hellocdpandroid.ui.logs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.salesforce.marketingcloud.hellocdpandroid.HelloCdpApp

class SdkLogsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = HelloCdpApp.logOutput.toString().replace(", ", "")
    }
    val text: LiveData<String> = _text
}