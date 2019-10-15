package com.othings.technologies.bluetooth.bluetoothScanning;

public enum BluetoothFilter {

    PRINTERS(1664);
    int value;

    BluetoothFilter( int value ){
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
