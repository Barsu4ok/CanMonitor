package com.daniil.canmonitor.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.daniil.canmonitor.MainActivity;
import com.daniil.canmonitor.adapter.BtConsts;

import java.nio.charset.StandardCharsets;

public class BtConnection {
    private Context context;
    private SharedPreferences pref;
    private BluetoothAdapter btAdapter;
    private BluetoothDevice device;
    private ConnectThread connectThread;
    public static String MAC = "";
    public ConnectThread getConnectThread() {
        return connectThread;
    }

    public BtConnection(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(BtConsts.MY_PREF,Context.MODE_PRIVATE);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    public void connect(){
        String mac = pref.getString(BtConsts.MAC_KEY,"");
        MAC = mac;
        if(!btAdapter.isEnabled()){
            Toast.makeText(context, "Включите bluetooth для работы с устройством!", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(mac.isEmpty()){
            Toast.makeText(context, "Выберите bluetooth устройство для работы с ним!", Toast.LENGTH_SHORT).show();
            return;
        }
        device = btAdapter.getRemoteDevice(mac);
        if(device == null){
            MainActivity.CHC = false;
            return;
        }
        else if(device != null){
            connectThread = new ConnectThread(context,btAdapter,device);
            connectThread.start();
        }
    }


    public void sendMessage(String message){
        connectThread.getRThread().sendMessage(message.getBytes());
    }
}
