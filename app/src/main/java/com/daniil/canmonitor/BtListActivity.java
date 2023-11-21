package com.daniil.canmonitor;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.daniil.canmonitor.adapter.BtAdapter;
import com.daniil.canmonitor.adapter.ListItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BtListActivity extends AppCompatActivity {

    private final int BT_REQUEST_PERM = 111;
    private static Set<ListItem> listItemsDiscovery;
    private ListView listView;
    private BtAdapter adapter;

    private List<ListItem> list;

    private BluetoothAdapter btAdapter;

    private boolean isBtPermissionGranted = false;

    private boolean isDiscovering = false;

    private ActionBar ab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_list);
        getSupportActionBar().setTitle("CAN Monitor J1939");
        getBtPermission();
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bt_list_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("MissingPermission")
    private void enableSearch(){
        if(btAdapter.isDiscovering()){
            btAdapter.cancelDiscovery();
        }else {
            btAdapter.startDiscovery();
            ab.setTitle(R.string.discovery);
            isDiscovering = true;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            if(isDiscovering){
                btAdapter.cancelDiscovery();
                isDiscovering = false;
                getPairedDevices();
            }
            else {
                finish();
            }
        }
        else if(item.getItemId() == R.id.id_search){
            boolean flag = false;
            for (ListItem item1 : list) {
                if(item1.getItemType().equals(BtAdapter.TITLE_ITEM_TYPE)){
                    flag = true;
                }
            }
            if(flag){
                enableSearch();
            }else {
                ListItem itemTitle = new ListItem();
                itemTitle.setItemType(BtAdapter.TITLE_ITEM_TYPE);
                list.add(itemTitle);
                adapter.notifyDataSetChanged();
                enableSearch();
            }
        }
        return super.onOptionsItemSelected(item);
    }

        private void init(){
        ab = getSupportActionBar();
        listItemsDiscovery = new HashSet<>();
        //Для новых версий SDK
        //BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        //btAdapter = bluetoothManager.getAdapter();
            //Для старых версий SDK
            btAdapter = BluetoothAdapter.getDefaultAdapter();
        list = new ArrayList<>();
        //ActionBar ab = getSupportActionBar();
        if(ab == null) return;
        ab.setDisplayHomeAsUpEnabled(true);
        listView = findViewById(R.id.listView);
        adapter = new BtAdapter(this,R.layout.bt_list_item,list);
        listView.setAdapter(adapter);
        getPairedDevices();
        onItemClickListener();
    }

    @SuppressLint("MissingPermission")
    private void getPairedDevices(){
        @SuppressLint("MissingPermission") Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            list.clear();
            for (BluetoothDevice device : pairedDevices) {
                ListItem item = new ListItem();
                item.setBtDevice(device);
                list.add(item);
                adapter.notifyDataSetChanged();
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == BT_REQUEST_PERM){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                isBtPermissionGranted = true;
            }else {
                Toast.makeText(this, "Нет разрешения на поиск bluetooth устройств!", Toast.LENGTH_SHORT).show();
            }
        }else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void getBtPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},BT_REQUEST_PERM);
        }else{
            isBtPermissionGranted = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter f1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter f2 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        IntentFilter f3 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(breceiver, f1);
        registerReceiver(breceiver, f2);
        registerReceiver(breceiver, f3);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(breceiver);
    }


    private final BroadcastReceiver breceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device != null) {
                    ListItem item = new ListItem();
                    item.setBtDevice(device);
                    item.setItemType(BtAdapter.DISCOVERY_ITEM_TYPE);
                    if(!listItemsDiscovery.contains(item)){
                        if(!(item.getBtDevice().getName() == null)) {
                            listItemsDiscovery.add(item);
                            list.add(item);
                            adapter.notifyDataSetChanged();
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
                adapter.notifyDataSetChanged();
            } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())){
                isDiscovering = false;
                ab.setTitle(R.string.app_name);
            }
            else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() == BluetoothDevice.BOND_BONDED){
                    getPairedDevices();
                    adapter.notifyDataSetChanged();
                }
            }
        }
    };

    private void onItemClickListener(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint({"MissingPermission", "NewApi"})
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListItem item = (ListItem) parent.getItemAtPosition(position);
                if(item.getItemType().equals(BtAdapter.DISCOVERY_ITEM_TYPE)){
                    item.getBtDevice().createBond();
                    adapter.clear();
                    getPairedDevices();
                    adapter.notifyDataSetChanged();
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

}