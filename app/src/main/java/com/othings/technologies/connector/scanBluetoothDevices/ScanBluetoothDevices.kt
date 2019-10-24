package com.othings.technologies.connector.scanBluetoothDevices

import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.othings.technologies.bluetooth.Bluetooth
import com.othings.technologies.bluetooth.bluetoothBond.BluetoothBondState
import com.othings.technologies.bluetooth.bluetoothCommunication.BluetoothClient
import com.othings.technologies.bluetooth.bluetoothScanning.BluetoothFilter
import com.othings.technologies.connector.R
import com.othings.technologies.connector.databinding.ActivityScanBluetoothDevicesBinding
import com.othings.technologies.connector.showBondedDevices.adapters.BluetoothDeviceAdapter

class ScanBluetoothDevices : AppCompatActivity() {

    private lateinit var viewmodel:ScanBluetoothDevicesViewModel
    private lateinit var binding:ActivityScanBluetoothDevicesBinding
    private lateinit var adapter:BluetoothDeviceAdapter
    private lateinit var bluetoothDevices :MutableList<BluetoothDevice>
    private lateinit var bluetooth: Bluetooth
    private lateinit var observer:Observer<BluetoothDevice>
    private lateinit var sendDataObserver:Observer<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewmodel = ViewModelProviders.of(this).get(ScanBluetoothDevicesViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_scan_bluetooth_devices)

        bluetoothDevices = ArrayList()
        adapter = BluetoothDeviceAdapter(bluetoothDevices)
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = adapter

        bluetooth = Bluetooth(this);

        observer = Observer<BluetoothDevice>{bluetoothDevice->

            bluetoothDevices.add(bluetoothDevice)
            adapter.notifyDataSetChanged()

        }

        sendDataObserver = Observer<String>{status->

            Snackbar.make(binding.contextView,status,Snackbar.LENGTH_SHORT).show()

        }


        bluetooth.scanning.getFilteredDevices(BluetoothFilter.PRINTERS,10000).observe(this, observer)

        adapter.setOnClickItemListener().observe(this, Observer<Int> { position ->

            var bluetoothDevice = bluetoothDevices.get(position)
            bluetooth.bond.bondDevice(bluetoothDevice).observe(this,Observer<BluetoothBondState>{state->

                if(state.state == BluetoothDevice.BOND_BONDING){
                    Snackbar.make(binding.contextView,"BONDING",Snackbar.LENGTH_SHORT).show()
                }
                else if( state.state == BluetoothDevice.BOND_BONDED ){
                    Snackbar.make(binding.contextView,"BONDED",Snackbar.LENGTH_SHORT).show()
                }
                else if( state.state == BluetoothDevice.BOND_NONE ){
                    Snackbar.make(binding.contextView,"NOT_BONDED",Snackbar.LENGTH_SHORT).show()
                }

            })


        })

        binding.searchDevices.setOnClickListener {

            bluetoothDevices.clear()
            adapter.notifyDataSetChanged()
            bluetooth.scanning.getFilteredDevices(BluetoothFilter.PRINTERS,10000).observe(this, observer)

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        bluetooth.onActivityResult(requestCode,resultCode,data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        bluetooth.onRequestPermissionsResult(requestCode,permissions,grantResults)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

}
