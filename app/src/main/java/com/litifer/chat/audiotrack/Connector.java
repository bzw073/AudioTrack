package com.litifer.chat.audiotrack;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.IBluetoothA2dp;
import android.bluetooth.IBluetoothHeadset;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.Set;


public class Connector extends Service {
    private static IBluetoothHeadset ibht;
    private static IBluetoothA2dp ibta;
    public static Context application;
    private static String LOG_TAG = "HSP_Connector";
    private static BluetoothDevice device = null;


    public void onCreate() {
         super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "OnDestroy called");
        super.onDestroy();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        for (BluetoothDevice dev : pairedDevices) {
            if (dev.getAddress().equalsIgnoreCase(getString(R.string.toy_address)))
                device=dev;
        }
        if (ibht!=null){
            setHSPConnection();
            return START_NOT_STICKY;
        }
        getIBluetoothHeadset(getApplication());
//
//        if (ibta!=null){
//            setA2DPConnection();
//            return START_NOT_STICKY;
//        }
//
//        getIBluetoothA2dp(getApplication());
        return START_NOT_STICKY;
    }

    //HSP
    public void getIBluetoothHeadset(Context context) {
        Intent i = new Intent(IBluetoothHeadset.class.getName());
        i.setPackage(getPackageManager().resolveService(i, PackageManager.GET_RESOLVED_FILTER).serviceInfo.packageName);
        if (bindService(i, HSPConnection, Context.BIND_AUTO_CREATE)) {
            Log.i("HSP SUCCEEDED", "HSP connection bound");
        } else {
            Log.e("HSP FAILED", "Could not bind to Bluetooth HFP Service");
        }
    }

    //Method for bind
    public static ServiceConnection HSPConnection= new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
                    ibht = IBluetoothHeadset.Stub.asInterface(service);
                    setHSPConnection();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ibht=null;
        }

    };

    private static void setHSPConnection(){
        try {
            ibht.connect(device);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    //A2DP
    public void getIBluetoothA2dp(Context context) {
        Intent i = new Intent(IBluetoothA2dp.class.getName());
        i.setPackage(getPackageManager().resolveService(i, PackageManager.GET_RESOLVED_FILTER).serviceInfo.packageName);
        if (bindService(i, A2DPConnection, Context.BIND_AUTO_CREATE)) {
            Log.i("A2DP SUCCEEDED", "HSP connection bound");
        } else {
            Log.e("A2DP FAILED", "Could not bind to Bluetooth HFP Service");
        }
    }

    public static ServiceConnection A2DPConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ibta=IBluetoothA2dp.Stub.asInterface(service);
            setA2DPConnection();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ibta=null;
        }
    };

    private static void setA2DPConnection(){
        try {
            ibta.connect(device);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
