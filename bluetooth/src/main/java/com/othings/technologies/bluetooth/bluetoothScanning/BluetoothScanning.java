package com.othings.technologies.bluetooth.bluetoothScanning;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.lifecycle.MutableLiveData;
import java.util.ArrayList;
import java.util.List;

public class BluetoothScanning {

    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private MutableLiveData<BluetoothDevice> devices;
    private MutableLiveData<BluetoothDevice> devicesWithFilter;
    private MutableLiveData<BluetoothDevice> device;
    private BluetoothFilter bluetoothFilter;
    private BluetoothAction bluetoothAction;
    private BluetoothDevice bluetoothDevice;
    private String macAddress;
    private List<BluetoothDevice> bluetoothDevices;
    private MutableLiveData<String> status;

    public BluetoothScanning(Context context){

        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        context.registerReceiver(broadcastReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(broadcastReceiver, filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(broadcastReceiver, filter);
        bluetoothDevices = new ArrayList<>();
        status = new MutableLiveData<>();
        devices = new MutableLiveData<>();
        devicesWithFilter = new MutableLiveData<>();
        device = new MutableLiveData<>();

    }

    public MutableLiveData<BluetoothDevice> getDevice( String macAddress ){

        status.setValue(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        boolean found = false;
        for( BluetoothDevice bluetoothDevice : bluetoothAdapter.getBondedDevices() ){
            if( bluetoothDevice.getAddress().equals(macAddress) ){
                found = true;
                device.setValue(bluetoothDevice);
                status.setValue(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            }
        }

        if( !found ){

            this.macAddress = macAddress;
            bluetoothDevices.clear();
            bluetoothAction = BluetoothAction.FIND_DEVICE;

            if( bluetoothAdapter.isDiscovering() ){
                bluetoothAdapter.cancelDiscovery();
            }

            bluetoothAdapter.startDiscovery();

        }

        return device;
    }
    public MutableLiveData<BluetoothDevice> getDevices() {

        bluetoothAction = BluetoothAction.SCAN_DEVICES;
        if( bluetoothAdapter.isDiscovering() ){
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();
        return devices;
    }
    public MutableLiveData<BluetoothDevice> getFilteredDevices( BluetoothFilter filter ) {

        this.bluetoothFilter = filter;
        bluetoothAction = BluetoothAction.SCAN_DEVICES;
        if( bluetoothAdapter.isDiscovering() ){
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();
        return devicesWithFilter;
    }
    private boolean hasDevice( BluetoothDevice bluetoothDevice ){

        for( BluetoothDevice device : bluetoothDevices ){

            if( device.getAddress().equals(bluetoothDevice) ){
                return true;
            }

        }

        return false;
    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            switch (action){

                case BluetoothDevice.ACTION_FOUND:{

                    BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    if( bluetoothAction == BluetoothAction.SCAN_DEVICES ){

                        if( bluetoothFilter != null ){

                            if( bluetoothDevice.getBluetoothClass().getDeviceClass() == bluetoothFilter.getValue()  ){

                                if( !hasDevice(bluetoothDevice) ){
                                    bluetoothDevices.add(bluetoothDevice);
                                    devicesWithFilter.setValue(bluetoothDevice);
                                    status.setValue(BluetoothDevice.ACTION_FOUND);
                                }

                            }

                        }else{

                            if( !hasDevice(bluetoothDevice) ){
                                bluetoothDevices.add(bluetoothDevice);
                                devices.setValue(bluetoothDevice);
                                status.setValue(BluetoothDevice.ACTION_FOUND);
                            }

                        }

                    }
                    else if( bluetoothAction == BluetoothAction.FIND_DEVICE ){

                            if( macAddress.equals(bluetoothDevice.getAddress()) ){
                                BluetoothScanning.this.bluetoothDevice = bluetoothDevice;
                                device.setValue(bluetoothDevice);
                            }
                    }

                    break;
                }
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:{
                    status.setValue(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                    break;
                }
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:{

                    status.setValue(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    if( bluetoothAction == BluetoothAction.FIND_DEVICE ){

                        if( BluetoothScanning.this.bluetoothDevice == null ){
                            device.setValue(null); // DEVICE NOT FOUND
                        }

                    }

                    bluetoothFilter = null;
                    bluetoothAction = null;
                    BluetoothScanning.this.bluetoothDevice = null;
                    bluetoothDevices.clear();

                    break;
                }

            }


        }
    };
}
