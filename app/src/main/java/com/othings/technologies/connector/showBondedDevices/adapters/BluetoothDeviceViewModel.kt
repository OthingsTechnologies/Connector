package com.othings.technologies.connector.showBondedDevices.adapters

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BluetoothDeviceViewModel :ViewModel() {

    lateinit var name:MutableLiveData<String>
    lateinit var macAddress:MutableLiveData<String>


}