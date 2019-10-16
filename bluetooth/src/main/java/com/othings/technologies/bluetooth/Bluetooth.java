package com.othings.technologies.bluetooth;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.othings.technologies.bluetooth.bluetoothBond.BluetoothBond;
import com.othings.technologies.bluetooth.bluetoothCommunication.BluetoothClient;
import com.othings.technologies.bluetooth.bluetoothScanning.BluetoothScanning;

public class Bluetooth implements PreferenceManager.OnActivityResultListener , ActivityCompat.OnRequestPermissionsResultCallback {

    private Context context;
    private BluetoothBond bond;
    private BluetoothScanning scanning;
    private BluetoothClient client;
    private String [] permissions = {
            "android.permission.BLUETOOTH",
            "android.permission.BLUETOOTH_ADMIN",
            "android.permission.ACCESS_COARSE_LOCATION"
    };
    private static final int REQUEST_PERMISSIONS = 3000;

    public Bluetooth(Context context){

        this.context = context;
        this.bond = new BluetoothBond(context);
        this.scanning = new BluetoothScanning(context);
        this.client = new BluetoothClient(context,bond,scanning);

        if( !hasPermissions() ){
            requestPermissions();
        }

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

    private void requestPermissions(){

        ActivityCompat.requestPermissions((AppCompatActivity) context,
                permissions,
                REQUEST_PERMISSIONS);

    }
    private boolean hasPermission( String permission ){

        return ContextCompat.checkSelfPermission((AppCompatActivity) context, permission ) == PackageManager.PERMISSION_GRANTED;

    }
    private boolean hasPermissions(){

        for( String permission : permissions ){

            if( !hasPermission(permission) ){
                return false;
            }

        }

        return true;
    }


    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {

        bond.onActivityResult(requestCode,resultCode,data);
        scanning.onActivityResult(requestCode,resultCode,data);
        client.onActivityResult(requestCode,resultCode,data);

        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if( requestCode == REQUEST_PERMISSIONS ){

            if( !verifyPermissions(grantResults) ){

                requestPermissions();

            }

        }

    }

    private boolean verifyPermissions( @NonNull int[] grantResults ){

        if( grantResults[0] == PackageManager.PERMISSION_DENIED ){
            return false;
        }
        if( grantResults[1] == PackageManager.PERMISSION_DENIED ){
            return false;
        }
        if( grantResults[2] == PackageManager.PERMISSION_DENIED ){
            return false;
        }
        return true;
    }

}
