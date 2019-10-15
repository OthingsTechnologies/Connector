package com.othings.technologies.bluetooth;

import android.annotation.SuppressLint;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnector implements ActivityCompat.OnRequestPermissionsResultCallback {

    private Context context;
    private LifecycleOwner lifecycleOwner;
    private BluetoothAdapter bluetoothAdapter;
    private Handler pairingHandler;

    /*
        PERMISSIONS
     */

    private String [] permissions = {
            "android.permission.BLUETOOTH",
            "android.permission.BLUETOOTH_ADMIN",
            "android.permission.ACCESS_COARSE_LOCATION"
    };

    /*
        OBSERVABLES
     */

    private MutableLiveData<String> bluetoothStatus; // PARA EL ESTATUS DE LOS PROCESOS BLUETOOTH
    private MutableLiveData<Throwable> handleErrors; // PARA EL MANEJO DE ERRORES
    private MutableLiveData<BluetoothDevice> bluetoothDevice; // PARA ENCONTRAR UN DISPOSITIVO CONCRETO
    private MutableLiveData<BluetoothDevice> linkDevice; // PARA PAREAR UN DSIPOSITIVO
    private MutableLiveData<BluetoothDevice> bluetoothDevices; // PARA ENCONTRAR UN DISPOSITIVO CONCRETO
    private MutableLiveData<List<BluetoothDevice>> bondedDevices; // PARA LOS DISPOSITIVOS EMPAREJADOS
    private MutableLiveData<String> bluetoothResponses;

    /*
        ACTIONS
     */

    private static final String BLUETOOTH_PERMISSION_REQUIRED_ERROR = "BLUETOOTH_PERMISSION_REQUIRED_ERROR";
    public static final String BLUETOOTH_ADMIN_PERMISSION_REQUIRED_ERROR = "BLUETOOTH_ADMIN_PERMISSION_REQUIRED_ERROR";
    public static final String ACCESS_COARSE_LOCATION_PERMISSION_REQUIRED_ERROR = "ACCESS_COARSE_LOCATION_PERMISSION_REQUIRED_ERROR";
    public static final String ACTION_FOUND = "ACTION_FOUND";
    public static final String ACTION_BLUETOOTH_STATE_ON = "ACTION_BLUETOOTH_STATE_ON";
    public static final String ACTION_BLUETOOTH_STATE_OFF = "ACTION_BLUETOOTH_STATE_OFF";
    public static final String ACTION_PAIRING_REQUEST = "ACTION_PAIRING_REQUEST";
    public static final String ACTION_BOND_STATE_BONDED = "ACTION_BOND_STATE_BONDED";
    public static final String ACTION_BOND_STATE_BONDED_NONE = "ACTION_BOND_STATE_BONDED_NONE";
    public static final String ACTION_DISCOVERY_STARTED = "ACTION_DISCOVERY_STARTED";
    public static final String ACTION_DISCOVERY_FINISHED = "ACTION_DISCOVERY_FINISHED";

    public static final String DATA_SENDED_OK = "DATA_SENDED_OK";
    public static final String DATA_SENDED_FAILED = "DATA_SENDED_FAILED";

    /*
        FILTERING
     */

    public static final int FILTER_ONLY_PRINTERS = 1664;

    /*

     */

    private boolean BONDING_DEVICE;
    private boolean SENDING_DATA;
    private boolean DEVICE_BONDED;
    private Action action;
    private int [] FILTERS;
    int TIMEOUT;
    private enum Action{

        SCANNING_DEVICES,
        SEARCHING_DEVICE,
        FILTERING_DEVICES,
        LINKING_DEVICE

    }
    private byte [] DATA;
    private String MAC_ADDRESS;
    private boolean BLUETOOTH_DEVICE_FOUND;
    private List<BluetoothDevice> bluetoothDevicesList;
    private List<Integer> filters;

    private static final int ACTION_REQUEST_PERMISSIONS = 3000;
    private static final int ACTION_GET_PAIRED_DEVICES = 3001;
    private static final int ACTION_SCAN_BLUETOOTH_DEVICES_WITH_FILTERS = 3002;
    private static final int ACTION_SCAN_BLUETOOTH_DEVICES_WITH_FILTERS_AND_TIMEOUT = 3003;
    private static final int ACTION_SCAN_BLUETOOTH_DEVICES = 3004;
    private static final int ACTION_SCAN_BLUETOOTH_DEVICES_WITH_TIMEOUT = 3005;
    private static final int ACTION_PAIR_DEVICE = 3006;

    public BluetoothConnector(Context context, LifecycleOwner lifecycleOwner){

        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothResponses = new MutableLiveData<>();
        bondedDevices = new MutableLiveData<>();
        this.bluetoothStatus = new MutableLiveData<String>();
        this.handleErrors = new MutableLiveData<Throwable>();
        this.bluetoothDevice = new MutableLiveData<BluetoothDevice>();
        this.bluetoothDevices = new MutableLiveData<BluetoothDevice>();
        this.linkDevice = new MutableLiveData<BluetoothDevice>();
        this.bluetoothDevicesList = new ArrayList<>();
        this.filters = new ArrayList<>();
        action = null;

        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        context.registerReceiver(receiver,filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(receiver,filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        context.registerReceiver(receiver,filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(receiver,filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(receiver,filter);

    }
    public MutableLiveData<String> getBluetoothStatus(){
        return bluetoothStatus;
    }

    public MutableLiveData<Throwable> handleErrors() {
        return handleErrors;
    }

    /*
        OBTENER LOS DISPOSITIVOS PAREADOS
     */

    public MutableLiveData<List<BluetoothDevice>> getPairedDevices(){

        List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
        filters.clear();

        if( hasPermissions() ){

            if( bluetoothAdapter.isEnabled() ){

                Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
                bluetoothDevices.addAll(bondedDevices);
                this.bondedDevices.setValue(bluetoothDevices);

            }
            else{

                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((AppCompatActivity) context).startActivityForResult(enableBluetoothIntent,ACTION_GET_PAIRED_DEVICES);

            }

        }
        else{
            requestPermissions();
        }

        return bondedDevices;
    }

    /*
        ESCANEO DE DISPOSITIVOS BLUETOOTH
     */

    public MutableLiveData<BluetoothDevice> scanBluetoothDevicesWithFilters( int ...filters){

        FILTERS = filters;
        this.filters.clear();
        for( int filter : filters ){
            this.filters.add(filter);
        }

        if( hasPermissions() ){

            if( bluetoothAdapter.isEnabled() ){

                if( bluetoothAdapter.isDiscovering() ){
                    bluetoothAdapter.cancelDiscovery();
                }

                action = Action.FILTERING_DEVICES;
                bluetoothAdapter.startDiscovery();

            }
            else{

                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((AppCompatActivity) context).startActivityForResult(enableBluetoothIntent,ACTION_SCAN_BLUETOOTH_DEVICES_WITH_FILTERS);

            }

        }
        else{

            requestPermissions();

        }

        return bluetoothDevices;

    }
    public MutableLiveData<BluetoothDevice> scanBluetoothDevicesWithFiltersAndTimeOut( int timeout , int ...filters){

        FILTERS = filters;
        TIMEOUT = timeout;
        this.filters.clear();
        for( int filter : filters ){
            this.filters.add(filter);
        }

        if( hasPermissions() ){

            if( bluetoothAdapter.isEnabled() ){

                if( bluetoothAdapter.isDiscovering() ){
                    bluetoothAdapter.cancelDiscovery();
                }

                action = Action.FILTERING_DEVICES;
                bluetoothAdapter.startDiscovery();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        bluetoothAdapter.cancelDiscovery();

                    }
                },timeout);

            }
            else{

                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((AppCompatActivity) context).startActivityForResult(enableBluetoothIntent,ACTION_SCAN_BLUETOOTH_DEVICES_WITH_FILTERS_AND_TIMEOUT);

            }

        }
        else{

            requestPermissions();

        }

        return bluetoothDevices;

    }
    public MutableLiveData<BluetoothDevice> scanBluetoothDevices(){

        filters.clear();
        if( hasPermissions() ){

            if( bluetoothAdapter.isEnabled() ){

                if( bluetoothAdapter.isDiscovering() ){
                    bluetoothAdapter.cancelDiscovery();
                }
                action = Action.SCANNING_DEVICES;
                bluetoothAdapter.startDiscovery();

            }
            else{

                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((AppCompatActivity) context).startActivityForResult(enableBluetoothIntent,ACTION_SCAN_BLUETOOTH_DEVICES);

            }

        }
        else{

            requestPermissions();

        }

        return bluetoothDevices;

    }
    public MutableLiveData<BluetoothDevice> scanBluetoothDevicesWithTimeOut( int timeout ){

        TIMEOUT = timeout;
        filters.clear();
        if( hasPermissions() ){

            if( bluetoothAdapter.isEnabled() ){

                if( bluetoothAdapter.isDiscovering() ){
                    bluetoothAdapter.cancelDiscovery();
                }

                action = Action.SCANNING_DEVICES;
                bluetoothAdapter.startDiscovery();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        bluetoothAdapter.cancelDiscovery();

                    }
                },timeout);

            }
            else{

                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((AppCompatActivity) context).startActivityForResult(enableBluetoothIntent,ACTION_SCAN_BLUETOOTH_DEVICES_WITH_TIMEOUT);

            }

        }
        else{

            requestPermissions();

        }

        return bluetoothDevices;

    }

    /*
        EMPAREJAMIENTO Y DESEMPAREJAMIENTO DE DISPOSITIVOS BLUETOOTH
     */
    public MutableLiveData<BluetoothDevice> linkBluetoothDevice(BluetoothDevice bluetoothDevice){

        action = Action.LINKING_DEVICE;

        bluetoothDevice.createBond();
        DEVICE_BONDED = false;

        return linkDevice;

    }
    public void unlinkBluetoothDevice( BluetoothDevice bluetoothDevice ){

        try {
            Method method = bluetoothDevice.getClass().getMethod("removeBond",(Class[]) null);
            method.invoke(bluetoothDevice,(Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*
        BUSQUEDA DE DISPOSITOS BLUETOOTH
     */

    public MutableLiveData<BluetoothDevice> findBluetoothDevice(final String macAddress){

        final boolean[] found = {false};
        final BluetoothDevice[] b = {null};

        getPairedDevices().observe(lifecycleOwner, new Observer<List<BluetoothDevice>>() {
                    @Override
                    public void onChanged(List<BluetoothDevice> bluetoothDevices) {

                        for( BluetoothDevice bluetoothDevice : bluetoothDevices ){
                            if( bluetoothDevice.getAddress().equals(macAddress) ){
                                found[0] = true;
                                b[0] = bluetoothDevice;
                            }
                        }

                        if(found[0]){

                            bluetoothDevice.postValue(b[0]);
                            if(bluetoothDevice.hasActiveObservers()){
                                bluetoothDevice.removeObservers(lifecycleOwner);
                            }

                        }
                        else{
                            action = Action.SEARCHING_DEVICE;
                            BLUETOOTH_DEVICE_FOUND = false;
                            MAC_ADDRESS = macAddress;

                            if( bluetoothAdapter.isDiscovering() ){
                                bluetoothAdapter.cancelDiscovery();
                            }

                            bluetoothAdapter.startDiscovery();

                        }

                    }
        });

        return bluetoothDevice;

    }
    public MutableLiveData<String> sendData( BluetoothDevice bluetoothDevice , byte [] data ){

        if( bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED ){

            BluetoothClientManager bluetoothClientManager = new BluetoothClientManager(bluetoothDevice);
            bluetoothClientManager.requestData(data);

        }
        else{

            BONDING_DEVICE = true;
            DATA = data;
            linkBluetoothDevice(bluetoothDevice);

        }

        return bluetoothResponses;

    }
    private void requestPermissions(){

        ActivityCompat.requestPermissions((AppCompatActivity) context,
                permissions,
                ACTION_REQUEST_PERMISSIONS);

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
    private boolean handlePermissionErrors( @NonNull int[] grantResults ){

        if( grantResults[0] == PackageManager.PERMISSION_DENIED ){
            handleErrors.postValue(new Throwable(BLUETOOTH_PERMISSION_REQUIRED_ERROR));
            return false;
        }
        if( grantResults[1] == PackageManager.PERMISSION_DENIED ){
            handleErrors.postValue(new Throwable(BLUETOOTH_ADMIN_PERMISSION_REQUIRED_ERROR));
            return false;
        }
        if( grantResults[2] == PackageManager.PERMISSION_DENIED ){
            handleErrors.postValue(new Throwable(ACCESS_COARSE_LOCATION_PERMISSION_REQUIRED_ERROR));
            return false;
        }
        return true;
    }
    private boolean existBluetoothDeviceOnList(BluetoothDevice bluetoothDevice){

        for( BluetoothDevice device :bluetoothDevicesList ){

            if( bluetoothDevice.getAddress().equals(device.getAddress()) ){
                return true;
            }

        }
        return false;
    }
    private boolean hasFilter(BluetoothDevice bluetoothDevice){

        for( int filter : filters ){

            if( filter == bluetoothDevice.getBluetoothClass().getDeviceClass()){
                return true;
            }

        }

        return false;
    }
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(  BluetoothConnector.this.action != null ){

                    if( BluetoothConnector.this.action.hashCode() == Action.SEARCHING_DEVICE.hashCode() ){

                        if( MAC_ADDRESS.equals(device.getAddress()) ){
                            BLUETOOTH_DEVICE_FOUND = true;
                            bluetoothStatus.postValue(ACTION_FOUND);
                            bluetoothDevice.setValue(device);
                            bluetoothAdapter.cancelDiscovery();
                        }

                    }
                    else if(  BluetoothConnector.this.action.hashCode() == Action.FILTERING_DEVICES.hashCode() || action.hashCode() == Action.SCANNING_DEVICES.hashCode() ){

                        if( !existBluetoothDeviceOnList(device) ){

                            if( filters.size() == 0){
                                bluetoothStatus.postValue(ACTION_FOUND);
                                bluetoothDevicesList.add(device);
                                bluetoothDevices.setValue(device);
                            }
                            else{

                                if( hasFilter(device) ){
                                    bluetoothStatus.postValue(ACTION_FOUND);
                                    bluetoothDevicesList.add(device);
                                    bluetoothDevices.setValue(device);
                                }

                            }

                        }

                    }
                    else{

                        bluetoothDevices.postValue(device);

                    }

                }


            }
            else if( BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action) ){

                bluetoothStatus.postValue(ACTION_PAIRING_REQUEST);
                pairingHandler = new Handler();

                pairingHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if( !DEVICE_BONDED ){
                            linkDevice.setValue(null);
                        }

                    }

                },35000);

            }
            else if( BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action) ){

                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {

                    bluetoothStatus.postValue(ACTION_BOND_STATE_BONDED);
                    linkDevice.setValue(bluetoothDevice);
                    DEVICE_BONDED = true;
                    if( BONDING_DEVICE ){

                        BONDING_DEVICE = false;

                        if( SENDING_DATA ){
                            SENDING_DATA = false;
                            sendData(bluetoothDevice,DATA);
                        }

                    }

                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    bluetoothStatus.postValue(ACTION_BOND_STATE_BONDED_NONE);
                }

            }

            else if( BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action) ){

                bluetoothDevicesList.clear();
                bluetoothStatus.postValue(ACTION_DISCOVERY_STARTED);

            }
            else if( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action) ){

                bluetoothStatus.postValue(ACTION_DISCOVERY_FINISHED);

                if( BluetoothConnector.this.action != null ){

                    if( BluetoothConnector.this.action.hashCode() == Action.SEARCHING_DEVICE.hashCode() ){


                        if( !BLUETOOTH_DEVICE_FOUND ){
                            BLUETOOTH_DEVICE_FOUND = false;
                            bluetoothDevice.postValue(null);
                        }

                    }

                }


                action = null;

            }
            else if( BluetoothAdapter.ACTION_STATE_CHANGED.equals(action) ){

                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if( state == BluetoothAdapter.STATE_ON ){

                    bluetoothStatus.postValue(ACTION_BLUETOOTH_STATE_ON);

                }
                else if( state == BluetoothAdapter.STATE_OFF ){

                    bluetoothStatus.postValue(ACTION_BLUETOOTH_STATE_OFF);

                }

            }

        }

    };

    @SuppressLint("RestrictedApi")
    class BluetoothServerManager extends Thread {

        private boolean cancelled;
        private BluetoothServerSocket serverSocket;
        private UUID uuid;
        private MutableLiveData<String> status;
        private MutableLiveData<Throwable> errors;
        private MutableLiveData<String> data;
        public static final String ACTION_CANCELED = "CANCELED";

        public BluetoothServerManager(){

            status = new MutableLiveData<>();
            errors = new MutableLiveData<>();
            data = new MutableLiveData<>();

            uuid = UUID.randomUUID();
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if( bluetoothAdapter != null ){
                try {
                    this.serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("CasaLeyApp",uuid);
                    this.cancelled = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{

                this.serverSocket = null;
                this.cancelled = true;

            }

        }

        public MutableLiveData<String> getStatus() {
            return status;
        }

        public MutableLiveData<Throwable> getErrors() {
            return errors;
        }

        public MutableLiveData<String> getData() {
            return data;
        }

        @Override
        public void run() {

            BluetoothSocket bluetoothSocket;

            while (true){

                if( this.cancelled ){

                    status.setValue(ACTION_CANCELED);
                    break;
                }

                try{

                    bluetoothSocket = serverSocket.accept();

                }catch (IOException e){

                    errors.setValue(e);
                    break;
                }

                if( !this.cancelled && bluetoothSocket != null ){

                    InputStream inputStream = null;
                    OutputStream outputStream = null;

                    try {

                        inputStream = bluetoothSocket.getInputStream();
                        outputStream = bluetoothSocket.getOutputStream();
                        int available = inputStream.available();
                        byte [] bytes = new byte[available];
                        inputStream.read(bytes,0,available);
                        String data = new String(bytes);


                    } catch (IOException e) {
                        errors.setValue(e);
                        break;
                    }
                    finally {

                        try {

                            inputStream.close();
                            outputStream.close();
                            bluetoothSocket.close();

                        } catch (IOException e) {
                            errors.setValue(e);
                            break;
                        }

                    }

                }

            }

        }
    }

    class BluetoothClientManager{

        private BluetoothDevice bluetoothDevice;
        private BluetoothSocket bluetoothSocket;
        private UUID uuid;
        public static final String REFUSED_CONNECTION = "REFUSED_CONNECTION";

        public BluetoothClientManager(BluetoothDevice bluetoothDevice){

            this.bluetoothDevice = bluetoothDevice;
            this.uuid = UUID.fromString(String.valueOf(bluetoothDevice.getUuids()[0]));

        }

        public MutableLiveData<String> requestData(final byte [] data ){

            final MutableLiveData<String> mutableLiveData = new MutableLiveData();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

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

                            BluetoothConnector.this.bluetoothResponses.postValue(DATA_SENDED_OK);


                        }else{
                            handleErrors.postValue(new Throwable(REFUSED_CONNECTION));
                            BluetoothConnector.this.bluetoothResponses.postValue(DATA_SENDED_FAILED);
                        }

                    } catch (Exception e) {
                        handleErrors.postValue(e);
                        BluetoothConnector.this.bluetoothResponses.postValue(DATA_SENDED_FAILED);
                    }

                }
            });

            thread.start();

            return mutableLiveData;

        }

    }


    public void onActivityResult( int requestCode , int resultCode , Intent data){

            switch (requestCode){

                case ACTION_GET_PAIRED_DEVICES:{
                    getPairedDevices();
                    break;
                }
                case ACTION_SCAN_BLUETOOTH_DEVICES_WITH_FILTERS:{
                    scanBluetoothDevicesWithFilters(FILTERS);
                    break;
                }
                case ACTION_SCAN_BLUETOOTH_DEVICES_WITH_FILTERS_AND_TIMEOUT:{
                    scanBluetoothDevicesWithFiltersAndTimeOut(TIMEOUT,FILTERS);
                    break;
                }
                case ACTION_SCAN_BLUETOOTH_DEVICES:{
                    scanBluetoothDevices();
                    break;
                }
                case ACTION_SCAN_BLUETOOTH_DEVICES_WITH_TIMEOUT:{
                    scanBluetoothDevicesWithTimeOut(TIMEOUT);
                    break;
                }

            }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if( handlePermissionErrors(grantResults) ){

            switch (requestCode){

                case ACTION_GET_PAIRED_DEVICES:{
                    getPairedDevices();
                    break;
                }
                case ACTION_SCAN_BLUETOOTH_DEVICES_WITH_FILTERS:{
                    scanBluetoothDevicesWithFilters(FILTERS);
                    break;
                }
                case ACTION_SCAN_BLUETOOTH_DEVICES_WITH_FILTERS_AND_TIMEOUT:{
                    scanBluetoothDevicesWithFiltersAndTimeOut(TIMEOUT,FILTERS);
                    break;
                }
                case ACTION_SCAN_BLUETOOTH_DEVICES:{
                    scanBluetoothDevices();
                    break;
                }
                case ACTION_SCAN_BLUETOOTH_DEVICES_WITH_TIMEOUT:{
                    scanBluetoothDevicesWithTimeOut(TIMEOUT);
                    break;
                }

            }

        }

    }
    public void onDestroy(){

        if( receiver != null ){
            context.unregisterReceiver(receiver);
        }

    }

}
