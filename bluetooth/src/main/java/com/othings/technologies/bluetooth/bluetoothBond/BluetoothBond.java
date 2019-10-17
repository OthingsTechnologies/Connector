package com.othings.technologies.bluetooth.bluetoothBond;

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

import java.lang.reflect.Method;

public class BluetoothBond implements PreferenceManager.OnActivityResultListener {

    private Context context;
    private MutableLiveData<BluetoothBondState> bondDevice;
    private MutableLiveData<BluetoothBondState> unBondDevice;
    private BluetoothDevice bluetoothDevice;
    private BluetoothAdapter bluetoothAdapter;
    private static final int TURN_ON_BLUETOOTH_REQUEST_CODE = 3001;
    private BluetoothMethod bluetoothMethod;

    public BluetoothBond(Context context){

        bondDevice = new MutableLiveData<>();
        unBondDevice = new MutableLiveData<>();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        context.registerReceiver(broadcastReceiver, filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(broadcastReceiver, filter);
        context.registerReceiver(broadcastReceiver, filter);

    }

    public MutableLiveData<BluetoothBondState> bondDevice( BluetoothDevice bluetoothDevice ){

        this.bluetoothDevice = bluetoothDevice;
        bluetoothMethod = BluetoothMethod.BOND;

        if( bluetoothAdapter.isEnabled() ){
            bluetoothDevice.createBond();
        }
        else{
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((AppCompatActivity) context).startActivityForResult(enableBtIntent, TURN_ON_BLUETOOTH_REQUEST_CODE);

        }
        return bondDevice;
    }
    public MutableLiveData<BluetoothBondState> unBondDevice( BluetoothDevice bluetoothDevice ){

        this.bluetoothDevice = bluetoothDevice;
        bluetoothMethod = BluetoothMethod.UNBOND;

        if( bluetoothAdapter.isEnabled() ){

            try {
                Method method = bluetoothDevice.getClass().getMethod("removeBond",(Class[]) null);
                method.invoke(bluetoothDevice,(Object[]) null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        else{

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((AppCompatActivity) context).startActivityForResult(enableBtIntent, TURN_ON_BLUETOOTH_REQUEST_CODE);

        }

        return unBondDevice;
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            switch (action){

                case BluetoothDevice.ACTION_PAIRING_REQUEST:{

                    final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    device.setPin("1234".getBytes());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if( bluetoothDevice != null ){
                                if( bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED ){
                                    BluetoothBondState bluetoothBondState = new BluetoothBondState(device,BluetoothDevice.BOND_NONE);
                                    bondDevice.setValue(bluetoothBondState);
                                }
                            }

                        }
                    },10000);

                    break;
                }
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:{

                    BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                    if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) { //DISPOSITIVO VINCULADO
                        BluetoothBond.this.bluetoothDevice = bluetoothDevice;
                        bondDevice.setValue(new BluetoothBondState(bluetoothDevice,BluetoothDevice.BOND_BONDED));

                    } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){ //DISPOSITIVO DESVINCULADO
                        BluetoothBond.this.bluetoothDevice = bluetoothDevice;
                        unBondDevice.setValue(new BluetoothBondState(bluetoothDevice,BluetoothDevice.BOND_NONE));
                    }

                    break;
                }

            }

        }
    };

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {

        if( requestCode == TURN_ON_BLUETOOTH_REQUEST_CODE ) {

            if (bluetoothAdapter.isEnabled()) {

                switch (bluetoothMethod){

                    case BOND:{
                        bondDevice(bluetoothDevice);
                        break;
                    }
                    case UNBOND:{
                        unBondDevice(bluetoothDevice);
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
