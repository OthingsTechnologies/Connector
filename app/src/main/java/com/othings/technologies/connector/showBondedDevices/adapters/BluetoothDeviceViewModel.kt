package com.othings.technologies.connector.showBondedDevices.adapters

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.othings.technologies.bluetooth.BluetoothConnector

class BluetoothDeviceViewModel :ViewModel() {

    lateinit var name:MutableLiveData<String>
    lateinit var macAddress:MutableLiveData<String>


}