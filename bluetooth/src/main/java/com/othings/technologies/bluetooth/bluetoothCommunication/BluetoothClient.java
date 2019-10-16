package com.othings.technologies.bluetooth.bluetoothCommunication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.othings.technologies.bluetooth.bluetoothBond.BluetoothBond;
import com.othings.technologies.bluetooth.bluetoothBond.BluetoothBondState;
import com.othings.technologies.bluetooth.bluetoothScanning.BluetoothScanning;

import java.io.OutputStream;
import java.util.UUID;

public class BluetoothClient implements PreferenceManager.OnActivityResultListener {

    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothBond bluetoothBond;
    private BluetoothScanning scanning;
    private MutableLiveData<String> requestStatus;
    public static final String REQUEST_OK = "REQUEST_OK";
    public static final String DEVICE_NOT_FOUND = "DEVICE_NOT_FOUND";
    public static final String REFUSED_CONNECTION = "REFUSED_CONNECTION";
    public static final String DEVICE_NOT_PAIRED = "DEVICE_NOT_PAIRED";
    private static final int TURN_ON_BLUETOOTH_REQUEST_CODE = 3002;
    private BluetoothMethod bluetoothMethod;
    private BluetoothDevice bluetoothDevice;
    private byte [] data;
    private LifecycleOwner owner;
    private String macAddress;

    public BluetoothClient(Context context ,BluetoothBond bluetoothBond, BluetoothScanning scanning){

        this.context = context;
        this.bluetoothBond = bluetoothBond;
        this.scanning = scanning;
        this.requestStatus = new MutableLiveData<>();
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    public MutableLiveData<String> request(final BluetoothDevice bluetoothDevice, final byte [] data , final LifecycleOwner owner ){

        this.bluetoothDevice = bluetoothDevice;
        this.data = data;
        this.owner = owner;
        bluetoothMethod = BluetoothMethod.REQUEST_WITH_BLUETOOTH_DEVICE;

        if( bluetoothAdapter.isEnabled() ){

            if( bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED ){

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        BluetoothSocket bluetoothSocket;
                        UUID uuid = null;
                        if( bluetoothDevice.getUuids() != null ){
                            uuid = UUID.fromString(String.valueOf(bluetoothDevice.getUuids()[0]));

                            try {

                                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                                Looper.prepare();
                                bluetoothSocket.connect();

                                if( bluetoothSocket.isConnected() ){

                                    OutputStream outputStream = bluetoothSocket.getOutputStream();

                                    outputStream.write(data);
                                    Thread.sleep(100);
                                    bluetoothSocket.close();
                                    Looper.myLooper().quit();

                                    requestStatus.postValue(REQUEST_OK);

                                }
                                else{

                                    requestStatus.postValue(REFUSED_CONNECTION);

                                }


                            }catch (Exception e){

                                requestStatus.postValue(e.getMessage());

                            }

                        }
                        else{
                            requestStatus.postValue(DEVICE_NOT_PAIRED);
                        }
                    }
                });
                thread.start();

            }
            else{

                bluetoothBond.bondDevice(bluetoothDevice).observe(owner, new Observer<BluetoothBondState>() {
                    @Override
                    public void onChanged(BluetoothBondState bluetoothBondState) {

                        if( bluetoothBondState.getState() == BluetoothBondState.BONDED ){

                            request(bluetoothBondState.getBluetoothDevice(),data,owner);

                        }
                        else{

                            request(bluetoothDevice,data,owner);

                        }
                    }
                });

            }

        }
        else{

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((AppCompatActivity) context).startActivityForResult(enableBtIntent, TURN_ON_BLUETOOTH_REQUEST_CODE);

        }


        return requestStatus;
    }
    public MutableLiveData<String> request(final String macAddress, final byte [] data , final LifecycleOwner owner ){

        bluetoothMethod = BluetoothMethod.REQUEST_WITH_MAC_ADDRESS;
        this.macAddress = macAddress;
        this.data = data;
        this.owner = owner;

        scanning.getDevice(macAddress).observe(owner, new Observer<BluetoothDevice>() {
            @Override
            public void onChanged(final BluetoothDevice bluetoothDevice) {

                if( bluetoothDevice != null ){

                    if( bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED ){

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {

                                BluetoothSocket bluetoothSocket;
                                UUID uuid = UUID.fromString(String.valueOf(bluetoothDevice.getUuids()[0]));

                                try {

                                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                                    Looper.prepare();
                                    bluetoothSocket.connect();

                                    if( bluetoothSocket.isConnected() ){

                                        OutputStream outputStream = bluetoothSocket.getOutputStream();

                                        outputStream.write(data);
                                        Thread.sleep(100);
                                        bluetoothSocket.close();
                                        Looper.myLooper().quit();

                                        requestStatus.postValue(REQUEST_OK);

                                    }
                                    else{

                                        requestStatus.postValue(REFUSED_CONNECTION);

                                    }


                                }catch (Exception e){

                                    requestStatus.postValue(e.getMessage());

                                }

                            }
                        });
                        thread.start();

                    }
                    else{

                        bluetoothBond.bondDevice(bluetoothDevice).observe(owner, new Observer<BluetoothBondState>() {
                            @Override
                            public void onChanged(BluetoothBondState bluetoothBondState) {

                                if( bluetoothBondState.getState() == BluetoothBondState.BONDED ){

                                    request(bluetoothBondState.getBluetoothDevice(),data,owner);

                                }
                                else{

                                    request(bluetoothDevice,data,owner);

                                }
                            }
                        });

                    }

                }
                else{
                    requestStatus.setValue(DEVICE_NOT_FOUND);
                }
            }
        });


        return requestStatus;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {

        if( requestCode == TURN_ON_BLUETOOTH_REQUEST_CODE ) {

            if (bluetoothAdapter.isEnabled()) {

                switch (bluetoothMethod){

                    case REQUEST_WITH_BLUETOOTH_DEVICE:{
                        request(bluetoothDevice,BluetoothClient.this.data,owner);
                        break;
                    }
                    case REQUEST_WITH_MAC_ADDRESS:{
                        request(macAddress,BluetoothClient.this.data,owner);
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
