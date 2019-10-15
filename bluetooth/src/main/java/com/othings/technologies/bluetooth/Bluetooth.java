package com.othings.technologies.bluetooth;

import android.content.Context;

import com.othings.technologies.bluetooth.bluetoothBond.BluetoothBond;
import com.othings.technologies.bluetooth.bluetoothCommunication.BluetoothClient;
import com.othings.technologies.bluetooth.bluetoothScanning.BluetoothScanning;

public class Bluetooth {

    private Context context;
    private BluetoothBond bond;
    private BluetoothScanning scanning;
    private BluetoothClient client;

    public Bluetooth(Context context){

        this.context = context;
        this.bond = new BluetoothBond(context);
        this.scanning = new BluetoothScanning(context);
        this.client = new BluetoothClient(bond);


    }

    public Context getContext() {
        return context;
    }

    public BluetoothBond getBond() {
        return bond;
    }

    public BluetoothScanning getScanning() {
        return scanning;
    }

    public BluetoothClient getClient() {
        return client;
    }
}
