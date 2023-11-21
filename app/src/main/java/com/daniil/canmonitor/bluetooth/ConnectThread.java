package com.daniil.canmonitor.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.daniil.canmonitor.MainActivity;
import com.daniil.canmonitor.ReciveThread;

import java.io.IOException;
import java.util.UUID;

public class ConnectThread extends Thread{

    private boolean isConnected = false;
    private Context context;

    public boolean isConnected() {
        return isConnected;
    }

    private BluetoothAdapter btAdapter;
    private BluetoothDevice device;

    private ReciveThread rThread;
    private BluetoothSocket mSocket;


    public static final String UUID = "00001101-0000-1000-8000-00805F9B34FB";

    @SuppressLint("MissingPermission")
    public ConnectThread(Context context, BluetoothAdapter btAdapter, BluetoothDevice device){
        this.context = context;
        this.btAdapter = btAdapter;
        this.device = device;
        try {
            mSocket = device.createRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID));
        } catch (IOException  e){
            e.printStackTrace();
        }
    }

    public ReciveThread getRThread() {
        return rThread;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        btAdapter.cancelDiscovery();
        try {
            mSocket.connect();
            synchronized (this) {
                isConnected = true;
                MainActivity.CHC = true;
            }
            rThread = new ReciveThread(mSocket);
            rThread.start();
        } catch (IOException  e){
            MainActivity.CHC = false;
            closeConnection();
        }
    }

    public void closeConnection(){
        try {
            synchronized (this) {
                isConnected = false;
                MainActivity.CHC = false;
            }
            mSocket.close();
        }catch (IOException e){
            System.out.println("Ошибка во время передачи данных");
        }
    }
}
