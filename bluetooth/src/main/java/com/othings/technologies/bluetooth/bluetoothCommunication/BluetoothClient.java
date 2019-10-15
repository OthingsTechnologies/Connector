package com.othings.technologies.bluetooth.bluetoothCommunication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Looper;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.othings.technologies.bluetooth.bluetoothBond.BluetoothBond;
import com.othings.technologies.bluetooth.bluetoothBond.BluetoothBondState;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothClient {

    private BluetoothBond bluetoothBond;
    private MutableLiveData<String> requestStatus;
    public static final String REQUEST_OK = "REQUEST_OK";
    public static final String REFUSED_CONNECTION = "REFUSED_CONNECTION";

    public BluetoothClient(BluetoothBond bluetoothBond){

        this.bluetoothBond = bluetoothBond;
        this.requestStatus = new MutableLiveData<>();

    }

    public MutableLiveData<String> request(final BluetoothDevice bluetoothDevice, final byte [] data , final LifecycleOwner owner ){

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
        return requestStatus;
    }

}
