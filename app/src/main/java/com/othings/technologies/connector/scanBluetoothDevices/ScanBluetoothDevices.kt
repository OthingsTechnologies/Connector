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
import com.othings.technologies.bluetooth.BluetoothConnector
import com.othings.technologies.connector.R
import com.othings.technologies.connector.databinding.ActivityScanBluetoothDevicesBinding
import com.othings.technologies.connector.showBondedDevices.adapters.BluetoothDeviceAdapter

class ScanBluetoothDevices : AppCompatActivity() {

    private lateinit var viewmodel:ScanBluetoothDevicesViewModel
    private lateinit var binding:ActivityScanBluetoothDevicesBinding
    private lateinit var adapter:BluetoothDeviceAdapter
    private lateinit var bluetoothDevices :MutableList<BluetoothDevice>
    private lateinit var connector: BluetoothConnector
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewmodel = ViewModelProviders.of(this).get(ScanBluetoothDevicesViewModel::class.java)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_scan_bluetooth_devices)

        connector = BluetoothConnector(this,this)
        bluetoothDevices = ArrayList()
        adapter = BluetoothDeviceAdapter(bluetoothDevices)
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = adapter

        connector.bluetoothStatus.observe(this,Observer<String>{ status ->

            Snackbar.make(binding.contextView,status, Snackbar.LENGTH_SHORT).show()

        })

        connector.scanBluetoothDevicesWithFiltersAndTimeOut(10000,BluetoothConnector.FILTER_ONLY_PRINTERS).observe(this, Observer<BluetoothDevice>{ bluetoothDevice ->

            bluetoothDevices.add(bluetoothDevice)
            adapter.notifyDataSetChanged()

        })

        adapter.setOnClickItemListener().observe(this,Observer<Int>{ position ->

            var bluetoothDevice = bluetoothDevices.get(position)

            connector.findBluetoothDevice(bluetoothDevice.address).observe(this,Observer<BluetoothDevice>{bluetoothDevice->

                var b = bluetoothDevice;
                connector.linkBluetoothDevice(b).observe(this,Observer<BluetoothDevice>{linkedDevice->

                    var asds ="asdasd"

                });


            })


           /* var data = "^XA\n" +
                    "\n" +
                    "^FX Top section with company logo, name and address.\n" +
                    "^CF0,60\n" +
                    "^FO50,50^GB100,100,100^FS\n" +
                    "^FO75,75^FR^GB100,100,100^FS\n" +
                    "^FO88,88^GB50,50,50^FS\n" +
                    "^FO220,50^FDIntershipping, Inc.^FS\n" +
                    "^CF0,30\n" +
                    "^FO220,115^FD1000 Shipping Lane^FS\n" +
                    "^FO220,155^FDShelbyville TN 38102^FS\n" +
                    "^FO220,195^FDUnited States (USA)^FS\n" +
                    "^FO50,250^GB700,1,3^FS\n" +
                    "\n" +
                    "^FX Second section with recipient address and permit information.\n" +
                    "^CFA,30\n" +
                    "^FO50,300^FDJohn Doe^FS\n" +
                    "^FO50,340^FD100 Main Street^FS\n" +
                    "^FO50,380^FDSpringfield TN 39021^FS\n" +
                    "^FO50,420^FDUnited States (USA)^FS\n" +
                    "^CFA,15\n" +
                    "^FO600,300^GB150,150,3^FS\n" +
                    "^FO638,340^FDPermit^FS\n" +
                    "^FO638,390^FD123456^FS\n" +
                    "^FO50,500^GB700,1,3^FS\n" +
                    "\n" +
                    "^FX Third section with barcode.\n" +
                    "^BY5,2,270\n" +
                    "^FO100,550^BC^FD12345678^FS\n" +
                    "\n" +
                    "^FX Fourth section (the two boxes on the bottom).\n" +
                    "^FO50,900^GB700,250,3^FS\n" +
                    "^FO400,900^GB1,250,3^FS\n" +
                    "^CF0,40\n" +
                    "^FO100,960^FDCtr. X34B-1^FS\n" +
                    "^FO100,1010^FDREF1 F00B47^FS\n" +
                    "^FO100,1060^FDREF2 BL4H8^FS\n" +
                    "^CF0,190\n" +
                    "^FO470,955^FDCA^FS\n" +
                    "\n" +
                    "^XZ"

            connector.sendData(bluetoothDevice,data.toByteArray()).observe(this,Observer<String>{ data->

                Snackbar.make(binding.contextView,data, Snackbar.LENGTH_SHORT).show()

            })*/

        })


        connector.handleErrors().observe(this, Observer<Throwable> {error->

            Snackbar.make(binding.contextView,error.message.toString(), Snackbar.LENGTH_SHORT).show()

        })


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        connector.onActivityResult(requestCode,resultCode,data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        connector.onRequestPermissionsResult(requestCode,permissions,grantResults)

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        connector.onDestroy()
        super.onDestroy()
    }
}
