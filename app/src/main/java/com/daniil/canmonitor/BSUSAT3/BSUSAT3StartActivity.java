package com.daniil.canmonitor.BSUSAT3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.daniil.canmonitor.BtListActivity;
import com.daniil.canmonitor.MainActivity;
import com.daniil.canmonitor.R;
import com.daniil.canmonitor.adapter.BtConsts;
import com.daniil.canmonitor.adapter.ViewPagerAdapter;
import com.daniil.canmonitor.bluetooth.BtConnection;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class BSUSAT3StartActivity extends AppCompatActivity {

    private MenuItem menuItem;
    private MenuItem menuItemConnectionButton;
    private BluetoothAdapter btAdapter;
    public static boolean isConnection = false;
    private static final int ENABLE_REQUEST = 15;
    private static final int READ_REQUEST_CODE = 10;
    private SharedPreferences pref;
    public static BtConnection btConnection;

    private ViewPager2 viewPager2;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bsuast3_main);
        getSupportActionBar().setTitle("BSUSAT3 Manager");
        init();
        viewPager2 = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager2.setAdapter(viewPagerAdapter);
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Power Manager");
                    break;
                case 1:
                    tab.setText("Orientation");
                    break;
                case 2:
                    tab.setText("Camera");
                case 3:
                    tab.setText("GNSS");
                    break;
                case 4:
                    tab.setText("Radio");
                    break;
                case 5:
                    tab.setText("Solar");
                    break;
            }
        }).attach();
    }

    private void init(){
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        pref = getSharedPreferences(BtConsts.MY_PREF,MODE_PRIVATE);
        btConnection = new BtConnection(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bsusat3_menu,menu);
        menuItem = menu.findItem(R.id.id_bt_button);
        menuItemConnectionButton = menu.findItem(R.id.id_connect);
        setBtIcon();
        setConnectIcon();
        return super.onCreateOptionsMenu(menu);
    }

    private void setConnectIcon() {
        if(btAdapter.isEnabled() ) {
            if (isConnection) {
                menuItemConnectionButton.setIcon(R.drawable.ic_connection_enable);
            } else {
                menuItemConnectionButton.setIcon(R.drawable.ic_connection);
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.id_bt_button){
            if(!btAdapter.isEnabled()){
                enableBt();
            }
            else{
                btAdapter.disable();
                menuItem.setIcon(R.drawable.ic_bluetooth_enable);
            }
        }
        else if(item.getItemId() == R.id.id_menu){
            if(btAdapter.isEnabled()) {
                Intent i = new Intent(BSUSAT3StartActivity.this, BtListActivity.class);
                startActivity(i);
            }else{
                Toast.makeText(this, "Включите Bluetooth для перехода к bluetooth списку устройств!", Toast.LENGTH_SHORT).show();
            }
        } else if(item.getItemId() == R.id.id_connect){
            synchronized (this) {
                if(btAdapter.isEnabled()) {
                    if (btConnection.getConnectThread() != null) {
                        btConnection.getConnectThread().closeConnection();
                        setConnectIcon();
                        Toast.makeText(this, "Подключение для передачи и получения данных разорвано", Toast.LENGTH_SHORT).show();
                    } else {
                        btConnection.connect();
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        setConnectIcon();
                        permissionToTransfer();
                    }
                }else{
                    Toast.makeText(this, "Включите Bluetooth для установки соединения", Toast.LENGTH_SHORT).show();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void permissionToTransfer() {
        synchronized (this) {
            if(BtConnection.MAC.equals("")){
                Toast.makeText(this, "Выберите bluetooth устройство для работы с ним!", Toast.LENGTH_SHORT).show();
                return;
            }else {
                if (btConnection.getConnectThread() != null) {
                    if (btConnection != null && btConnection.getConnectThread() != null) {
                        Toast.makeText(getApplicationContext(), "Подключение для передачи и получения данных успешно установлено", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Не удалось подключиться для передачи и получения данных", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    return;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ENABLE_REQUEST){
            if(resultCode == RESULT_OK){
                setBtIcon();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void enableBt() {
        Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(i,ENABLE_REQUEST);
    }

    private void setBtIcon() {
        if(btAdapter.isEnabled()){
            menuItem.setIcon(R.drawable.ic_bluetooth_disable);
        }
        else{
            menuItem.setIcon(R.drawable.ic_bluetooth_enable);
        }
    }
}