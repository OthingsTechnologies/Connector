package com.othings.technologies.connector

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.othings.technologies.connector.databinding.ActivityMainBinding
import com.othings.technologies.connector.scanBluetoothDevices.ScanBluetoothDevices
import com.othings.technologies.connector.showBondedDevices.ShowBondedDevices

class MainActivity : AppCompatActivity() {

    private lateinit var model: MainActivityViewModel
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        binding.lifecycleOwner = this
        model = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        binding.viewmodel = model

        binding.showBondedDevicesButton.setOnClickListener {

            startActivity(Intent(this,ShowBondedDevices::class.java))

        }

        binding.scanDevicesButton.setOnClickListener {

            startActivity(Intent(this,ScanBluetoothDevices::class.java))

        }


    }
}
