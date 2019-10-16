package com.othings.technologies.bluetooth.bluetoothScanning;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import java.util.ArrayList;
import java.util.List;

public class BluetoothScanning implements PreferenceManager.OnActivityResultListener {

    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private MutableLiveData<BluetoothDevice> devices;
    private MutableLiveData<BluetoothDevice> devicesWithFilter;
    private MutableLiveData<BluetoothDevice> device;
    private BluetoothFilter bluetoothFilter;
    private BluetoothAction bluetoothAction;
    private BluetoothMethod bluetoothMethod;
    private BluetoothDevice bluetoothDevice;
    private String macAddress;
    private int timeOut;
    private List<BluetoothDevice> bluetoothDevices;
    private MutableLiveData<String> status;
    public static final int TURN_ON_BLUETOOTH_REQUEST_CODE = 3000;

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
        bluetoothMethod = BluetoothMethod.GET_DEVICE;
        this.macAddress = macAddress;
        if( bluetoothAdapter.isEnabled() ){

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

                bluetoothDevices.clear();
                bluetoothAction = BluetoothAction.FIND_DEVICE;

                if( bluetoothAdapter.isDiscovering() ){
                    bluetoothAdapter.cancelDiscovery();
                }

                bluetoothAdapter.startDiscovery();

            }

        }
        else{

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((AppCompatActivity) context).startActivityForResult(enableBtIntent, TURN_ON_BLUETOOTH_REQUEST_CODE);

        }

        return device;
    }
    public MutableLiveData<BluetoothDevice> getDevice( String macAddress ,int timeout ){
        bluetoothMethod = BluetoothMethod.GET_DEVICE_WITH_TIMEOUT;
        this.macAddress = macAddress;
        this.timeOut = timeout;
        if( bluetoothAdapter.isEnabled() ){

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


                bluetoothDevices.clear();
                bluetoothAction = BluetoothAction.FIND_DEVICE;

                if( bluetoothAdapter.isDiscovering() ){
                    bluetoothAdapter.cancelDiscovery();
                }

                bluetoothAdapter.startDiscovery();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bluetoothAdapter.cancelDiscovery();
                    }
                },timeout);

            }

        }
        else{

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((AppCompatActivity) context).startActivityForResult(enableBtIntent, TURN_ON_BLUETOOTH_REQUEST_CODE);

        }

        return device;
    }
    public MutableLiveData<BluetoothDevice> getAllDevices() {
        bluetoothMethod = BluetoothMethod.GET_ALL_DEVICES;
        bluetoothAction = BluetoothAction.SCAN_DEVICES;

        if( bluetoothAdapter.isEnabled() ){

            if( bluetoothAdapter.isDiscovering() ){
                bluetoothAdapter.cancelDiscovery();
            }

            bluetoothAdapter.startDiscovery();

        }
        else{

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((AppCompatActivity) context).startActivityForResult(enableBtIntent, TURN_ON_BLUETOOTH_REQUEST_CODE);

        }

        return devices;
    }
    public MutableLiveData<BluetoothDevice> getAllDevices(int timeout) {
        this.timeOut = timeout;
        bluetoothMethod = BluetoothMethod.GET_ALL_DEVICES_WITH_TIMEOUT;
        bluetoothAction = BluetoothAction.SCAN_DEVICES;

        if( bluetoothAdapter.isEnabled() ){

            if( bluetoothAdapter.isDiscovering() ){
                bluetoothAdapter.cancelDiscovery();
            }

            bluetoothAdapter.startDiscovery();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    bluetoothAdapter.cancelDiscovery();
                }
            },timeout);

        }
        else{

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((AppCompatActivity) context).startActivityForResult(enableBtIntent, TURN_ON_BLUETOOTH_REQUEST_CODE);

        }



        return devices;
    }
    public MutableLiveData<BluetoothDevice> getFilteredDevices( BluetoothFilter filter ) {
        bluetoothMethod = BluetoothMethod.GET_FILTERED_DEVICES;
        this.bluetoothFilter = filter;
        bluetoothAction = BluetoothAction.SCAN_DEVICES;

        if( bluetoothAdapter.isEnabled() ){

            if( bluetoothAdapter.isDiscovering() ){
                bluetoothAdapter.cancelDiscovery();
            }

            bluetoothAdapter.startDiscovery();

        }
        else{

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((AppCompatActivity) context).startActivityForResult(enableBtIntent, TURN_ON_BLUETOOTH_REQUEST_CODE);

        }

        return devicesWithFilter;
    }
    public MutableLiveData<BluetoothDevice> getFilteredDevices( BluetoothFilter filter ,int timeout) {
        bluetoothMethod = BluetoothMethod.GET_FILTERED_DEVICE_WITH_TIMEOUT;
        this.bluetoothFilter = filter;
        this.timeOut = timeout;
        bluetoothAction = BluetoothAction.SCAN_DEVICES;
        if( bluetoothAdapter.isEnabled() ){

            if( bluetoothAdapter.isDiscovering() ){
                bluetoothAdapter.cancelDiscovery();
            }

            bluetoothAdapter.startDiscovery();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    bluetoothAdapter.cancelDiscovery();
                }
            },timeout);

        }
        else{

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((AppCompatActivity) context).startActivityForResult(enableBtIntent, TURN_ON_BLUETOOTH_REQUEST_CODE);

        }

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

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {

        if( requestCode == TURN_ON_BLUETOOTH_REQUEST_CODE ){

            if( bluetoothAdapter.isEnabled() ){

                    switch (bluetoothMethod){

                        case GET_DEVICE:{
                            getDevice(macAddress);
                            break;
                        }
                        case GET_DEVICE_WITH_TIMEOUT:{
                            getDevice(macAddress,timeOut);
                            break;
                        }
                        case GET_ALL_DEVICES:{
                            getAllDevices();
                            break;
                        }
                        case GET_ALL_DEVICES_WITH_TIMEOUT:{
                            getAllDevices(timeOut);
                            break;
                        }
                        case GET_FILTERED_DEVICES:{
                            getFilteredDevices(bluetoothFilter);
                            break;
                        }
                        case GET_FILTERED_DEVICE_WITH_TIMEOUT:{
                            getFilteredDevices(bluetoothFilter,timeOut);
                            break;
                        }

                    }

            }
            else{

                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((AppCompatActivity) context).startActivityForResult(enableBtIntent, TURN_ON_BLUETOOTH_REQUEST_CODE);

            }

        }

        return false;
    }
}
