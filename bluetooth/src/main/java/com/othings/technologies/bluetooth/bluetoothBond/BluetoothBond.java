package com.othings.technologies.bluetooth.bluetoothBond;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import androidx.lifecycle.MutableLiveData;

import java.lang.reflect.Method;

public class BluetoothBond {

    private Context context;
    private MutableLiveData<BluetoothBondState> bondDevice;
    private MutableLiveData<BluetoothBondState> unBondDevice;

    public BluetoothBond(Context context){

        bondDevice = new MutableLiveData<>();
        unBondDevice = new MutableLiveData<>();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        context.registerReceiver(broadcastReceiver, filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(broadcastReceiver, filter);

    }

    public MutableLiveData<BluetoothBondState> bondDevice( BluetoothDevice bluetoothDevice ){
        bluetoothDevice.createBond();
        return bondDevice;
    }

    public MutableLiveData<BluetoothBondState> unBondDevice( BluetoothDevice bluetoothDevice ){

        try {
            Method method = bluetoothDevice.getClass().getMethod("removeBond",(Class[]) null);
            method.invoke(bluetoothDevice,(Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return unBondDevice;
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            switch (action){

                case BluetoothDevice.ACTION_PAIRING_REQUEST:{

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            bondDevice.setValue(null);

                        }
                    },10000);

                    break;
                }
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:{

                    BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                    if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) { //DISPOSITIVO VINCULADO

                        bondDevice.setValue(new BluetoothBondState(bluetoothDevice,BluetoothDevice.BOND_BONDED));

                    } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){ //DISPOSITIVO DESVINCULADO

                        unBondDevice.setValue(new BluetoothBondState(bluetoothDevice,BluetoothDevice.BOND_NONE));

                    }

                    break;
                }

            }

        }
    };

}
