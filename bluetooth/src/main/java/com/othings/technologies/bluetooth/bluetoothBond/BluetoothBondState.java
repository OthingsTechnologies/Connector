package com.othings.technologies.bluetooth.bluetoothBond;

import android.bluetooth.BluetoothDevice;

public class BluetoothBondState {


    private BluetoothDevice bluetoothDevice;
    private int state;
    public static final int BONDED = BluetoothDevice.BOND_BONDED;
    public static final int NOT_BONDED = BluetoothDevice.BOND_NONE;

    public BluetoothBondState(BluetoothDevice bluetoothDevice, int state) {
        this.bluetoothDevice = bluetoothDevice;
        this.state = state;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public int getState() {
        return state;
    }
}
