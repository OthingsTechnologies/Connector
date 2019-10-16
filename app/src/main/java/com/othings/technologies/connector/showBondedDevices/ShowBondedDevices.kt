package com.othings.technologies.connector.showBondedDevices

import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.othings.technologies.connector.R
import com.othings.technologies.connector.databinding.ActivityShowBondedDevicesBinding
import com.othings.technologies.connector.showBondedDevices.adapters.BluetoothDeviceAdapter

class ShowBondedDevices : AppCompatActivity() {

    private lateinit var model: ShowBondedDevicesViewModel
    private lateinit var binding: ActivityShowBondedDevicesBinding
    private lateinit var adapter:BluetoothDeviceAdapter
    private lateinit var bluetoothDevices:MutableList<BluetoothDevice>
    //private lateinit var bluetoothConnector: BluetoothConnector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_show_bonded_devices)
        binding.lifecycleOwner = this
        model = ViewModelProviders.of(this).get(ShowBondedDevicesViewModel::class.java)
        binding.viewmodel = model


     /*  bluetoothConnector.pairedDevices.observe(this,Observer<MutableList<BluetoothDevice>>{result->

           bluetoothDevices = result
           adapter = BluetoothDeviceAdapter(bluetoothDevices)
           binding.recyclerview.layoutManager = LinearLayoutManager(this)
           binding.recyclerview.adapter = adapter

        })*/



       /* bluetoothConnector.bluetoothStatus.observe(this,Observer<String>{ status ->

            Snackbar.make(binding.contextView,status,Snackbar.LENGTH_SHORT).show()

        })*/

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
       // bluetoothConnector.onRequestPermissionsResult(requestCode,permissions,grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
      //  bluetoothConnector.onActivityResult(requestCode,resultCode,data)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}
