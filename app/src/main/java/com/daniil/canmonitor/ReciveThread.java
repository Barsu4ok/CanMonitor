package com.daniil.canmonitor;

import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;

import com.daniil.canmonitor.adapter.MesAdapter;
import com.daniil.canmonitor.parser.DBCParser;
import com.daniil.canmonitor.parser.Message;
import com.daniil.canmonitor.parser.Signal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class ReciveThread extends Thread{

    private BluetoothSocket socket;
    private InputStream inputS;

    private OutputStream outputS;

    private byte[] rBuffer;

    public ReciveThread(BluetoothSocket socket){
        this.socket = socket;
        try {
            inputS = socket.getInputStream();
        }catch (IOException e){
            e.printStackTrace();
        }
        try {
            outputS = socket.getOutputStream();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        rBuffer = new byte[1800];
        while (true){
            try {
                int size = inputS.read(rBuffer);
                String mes = new String(rBuffer,0,size);
                MainActivity.updatingListMessages(MainActivity.getAdapter(),mes,MainActivity.ct);
            }catch (IOException e){
                break;
            }catch (Exception e){
                break;
            }
        }
    }

    public void sendMessage(byte[] byteArray){
        try {
            outputS.write(byteArray);
        }catch (IOException e){

        }
    }
}
