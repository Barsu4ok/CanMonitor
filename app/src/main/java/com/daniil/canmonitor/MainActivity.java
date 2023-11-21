package com.daniil.canmonitor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.daniil.canmonitor.BSUSAT3.BSUSAT3StartActivity;
import com.daniil.canmonitor.adapter.BtConsts;
import com.daniil.canmonitor.adapter.MesAdapter;
import com.daniil.canmonitor.bluetooth.BtConnection;
import com.daniil.canmonitor.parser.DBCParser;
import com.daniil.canmonitor.parser.Message;
import com.daniil.canmonitor.parser.MessageCAN;
import com.daniil.canmonitor.parser.Signal;
import com.daniil.canmonitor.utils.ThisContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_RECEIVED_CAN_MESSAGE = "KEY_RECEIVED_CAN_MESSAGE";
    private static final String KEY_MESSAGES_CAN = "KEY_MESSAGES_CAN";
    private static final String KEY_POSITION_TO_NAME = "KEY_POSITION_TO_NAME";
    private static final String KEY_INDEX = "KEY_INDEX";
    private static final String KEY_FILE_DBC = "KEY_FILE_DBC";
    private static final String KEY_CUSTOM = "KEY_CUSTOM";

    public static String minCanId = "";
    public static String maxCanId = "";
    public static String mask = "";
    public static String CanId = "";
    public static String CanDLC = "";
    public static String CanData = "";
    public static String CanRTR = "";
    public static boolean CHC = false;
    public static int index;
    private boolean statusButtonStart = false;
    public static HashMap<String, Message> messagesDBC;
    public static HashMap<String, MessageCAN> messagesCAN;
    private static HashMap<String,String> positionToNameRatio;
    public static DBCParser parser;
    public static MesAdapter adapter;
    private MenuItem menuItem;
    private MenuItem menuItemConnectionButton;
    private ArrayList<String> messages;
    public static ArrayList<String> ct;
    private ProgressBar progressBar;
    private ListView listView;
    private Button buttonStart, buttonStop, buttonClear,buttonClose, buttonFilePicker, buttonSend;
    private Intent myFileIntent;
    public static Uri uri;
    public static String fileDBC = "default";
    public static boolean custom = false;
    private BluetoothAdapter btAdapter;
    public static boolean takeInboxes = true;
    public static boolean isStandart = true;
    private static final int ENABLE_REQUEST = 15;
    private static final int READ_REQUEST_CODE = 10;

    private SharedPreferences pref;

    public static BtConnection btConnection;

    public static boolean[] filtersIsActiv;
    private RadioButton radioButtonSTD;
    private RadioButton radioButtonEXT;

    public static Python python;
    public static PyObject pythonModule;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("CAN Monitor J1939");
        MyApplication.getInstance().setMainActivity(this);
        if(savedInstanceState != null) {
            messages = savedInstanceState.getStringArrayList(KEY_RECEIVED_CAN_MESSAGE);
            messagesCAN = (HashMap<String, MessageCAN>) savedInstanceState.getSerializable(KEY_MESSAGES_CAN);
            positionToNameRatio = (HashMap<String, String>) savedInstanceState.getSerializable(KEY_POSITION_TO_NAME);
            index = savedInstanceState.getInt(KEY_INDEX);
            custom = savedInstanceState.getBoolean(KEY_CUSTOM);
            fileDBC = savedInstanceState.getString(KEY_FILE_DBC);

        }
        init();
        buttonStart = findViewById(R.id.buttonStart);
        buttonStop = findViewById(R.id.buttonStop);
        buttonClear = findViewById(R.id.btnClear);
        buttonClose = findViewById(R.id.btnClose);
        buttonFilePicker = findViewById(R.id.btn_file_picker);
        buttonSend = findViewById(R.id.buttonSend);
        progressBar = findViewById(R.id.progressBar);
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btConnection.getConnectThread() != null && CHC && btConnection != null){
                    if(btConnection != null && btConnection.getConnectThread() != null && CHC != false) {
                        try {
                            if (filtersIsActiv != null && filtersIsActiv[0] == false) {
                                btConnection.sendMessage("Start|" + filtersIsActiv[2] + "|");
                                progressBar.setVisibility(View.VISIBLE);
                                Toast.makeText(MainActivity.this, "Начинаем приём данных", Toast.LENGTH_SHORT).show();
                                statusButtonStart = true;
                            } else if(filtersIsActiv != null && filtersIsActiv[0] == true) {
                                btConnection.sendMessage("Start|" + minCanId + "|" + maxCanId + "|" + filtersIsActiv[2] + "|");
                                progressBar.setVisibility(View.VISIBLE);
                                Toast.makeText(MainActivity.this, "Начинаем приём данных", Toast.LENGTH_SHORT).show();
                                statusButtonStart = true;
                            } else if(filtersIsActiv == null){
                                btConnection.sendMessage("Start|" + "true" + "|");
                                progressBar.setVisibility(View.VISIBLE);
                                Toast.makeText(MainActivity.this, "Начинаем приём данных", Toast.LENGTH_SHORT).show();
                                statusButtonStart = true;
                            }
                        }
                        catch (Exception e){
                            Log.i("myLog","" + e.getMessage());
                        }
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Сперва подключитель к устройству!", Toast.LENGTH_SHORT).show();
                    }
                } else{
                    Toast.makeText(MainActivity.this, "Сперва установите соединение для передачи данных, нажав соответствующую кнопку в меню", Toast.LENGTH_SHORT).show();
                }
            }
        });
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btConnection.getConnectThread() != null && btConnection != null && CHC) {
                    if (btConnection != null && CHC != false) {
                        btConnection.sendMessage("Stop");
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Приём данных остановлен", Toast.LENGTH_SHORT).show();
                        statusButtonStart = false;
                    } else {
                        Toast.makeText(MainActivity.this, "Сперва подключитель к устройству!", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(MainActivity.this, "Сперва установите соединение для передачи данных, нажав соответствующую кнопку в меню", Toast.LENGTH_SHORT).show();
                }
            }
        });
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearList(adapter);
            }
        });
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread.currentThread().interrupt();
                System.exit(0);
            }
        });
        buttonFilePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (statusButtonStart == true) {
                    Toast.makeText(MainActivity.this, "Прежде чем изменить DBC файл остановите приём данных", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Choosing a DBC file");
                    builder.setCancelable(false);
                    if (custom == false) {
                        builder.setMessage("default DBC");
                        builder.setPositiveButton("Default DBC", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (!fileDBC.equals("default")) {
                                    fileDBC = "default";
                                }
                                custom = false;
                                dialogInterface.cancel();
                                Toast.makeText(MainActivity.this, "Default DBC", Toast.LENGTH_SHORT).show();
                            }
                        });
                        builder.setNegativeButton("Pick file DBC", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                myFileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                myFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                                myFileIntent.setType("*/*");
                                startActivityForResult(myFileIntent, READ_REQUEST_CODE);
                                custom = true;
                            }
                        });
                        builder.setNeutralButton("close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                    } else {
                        builder.setMessage(fileDBC);
                        builder.setPositiveButton("Default DBC", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (!fileDBC.equals("default")) {
                                    fileDBC = "default";
                                }
                                custom = false;
                                dialogInterface.cancel();
                                Toast.makeText(MainActivity.this, "Default DBC", Toast.LENGTH_SHORT).show();
                            }
                        });
                        builder.setNegativeButton("Pick file DBC", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                myFileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                myFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                                myFileIntent.setType("*/*");
                                startActivityForResult(myFileIntent, READ_REQUEST_CODE);
                            }
                        });
                        builder.setNeutralButton("close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                    }
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInputDialog();
            }
        });
    }

    private void showInputDialog(){
        AlertDialog.Builder dialogBuilder = null;
        View dialogView = null;
        dialogBuilder = new AlertDialog.Builder(this);
        dialogView = getLayoutInflater().inflate(R.layout.input_dialog, null);

        dialogBuilder.setView(dialogView);
        EditText editTextCanId = dialogView.findViewById(R.id.editTextCanId);
        EditText editTextDLC = dialogView.findViewById(R.id.editTextCanDLC);
        EditText editTextData = dialogView.findViewById(R.id.editTextData);
        EditText editTextRtr = dialogView.findViewById(R.id.editTextCanRTR);
        radioButtonSTD = dialogView.findViewById(R.id.radioButtonSTD);
        radioButtonEXT = dialogView.findViewById(R.id.radioButtonEXT);
        Button clear = dialogView.findViewById(R.id.buttonClearFields);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CanId = "";
                CanDLC = "";
                CanData = "";
                CanRTR = "";
                editTextCanId.setText(CanId);
                editTextDLC.setText(CanDLC);
                editTextData.setText(CanData);
                editTextRtr.setText(CanRTR);
            }
        });
        editTextCanId.setText(CanId);
        editTextDLC.setText(CanDLC);
        editTextData.setText(CanData);
        editTextRtr.setText(CanRTR);
        setRadioButton();

        dialogBuilder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CanId = editTextCanId.getText().toString();
                CanDLC = editTextDLC.getText().toString();
                CanData = editTextData.getText().toString();
                CanRTR = editTextRtr.getText().toString();
                // Отправка данных в CAN шину
                if(btConnection.getConnectThread() != null && CHC && btConnection != null){
                    if(btConnection != null && btConnection.getConnectThread() != null && CHC != false) {
                        try {
                            btConnection.sendMessage("Send|" + CanId + "|" + CanDLC + "|" + CanData + "|" + CanRTR + "|" + isStandart + "|");
                            progressBar.setVisibility(View.VISIBLE);
                            Log.i("myLog","Send|" + CanId + "|" + CanDLC + "|" + CanData + "|" + CanRTR + "|" + isStandart + "|");
                            Toast.makeText(MainActivity.this, "Сообщение отправлено", Toast.LENGTH_SHORT).show();
                            statusButtonStart = true;
                        }
                        catch (Exception e){
                            Toast.makeText(MainActivity.this, "Ошибка отправки сообщения", Toast.LENGTH_SHORT).show();
                            Log.i("myLog","" + e.getMessage());
                        }
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Сперва подключитель к устройству!", Toast.LENGTH_SHORT).show();
                    }
                } else{
                    Toast.makeText(MainActivity.this, "Сперва установите соединение для передачи данных, нажав соответствующую кнопку в меню", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.radioButtonSTD:
                if (checked) {
                    isStandart = true;
                    radioButtonEXT.setChecked(false);
                }
                break;
            case R.id.radioButtonEXT:
                if (checked) {
                    isStandart = false;
                    radioButtonSTD.setChecked(false);
                }
                break;
        }
    }
    private void setRadioButton(){
        if(isStandart){
            radioButtonSTD.setChecked(true);
        } else radioButtonEXT.setChecked(true);
    }

    public void clearList(MesAdapter mesAdapter){
        messages.clear();
        messagesCAN.clear();
        mesAdapter.notifyDataSetChanged();
    }

    public static void updateList(MesAdapter mesAdapter){
        mesAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        menuItem = menu.findItem(R.id.id_bt_button);
        menuItemConnectionButton = menu.findItem(R.id.id_connect);
        setBtIcon();
        setConnectIcon();
        return super.onCreateOptionsMenu(menu);
    }

    public static void updatingListMessages(MesAdapter ad, String str, ArrayList<String> array){
        ArrayList<String> arr = array;
        String result = "";
        String[] recciveMessage = str.split("\\|");
        String id = DBCParser.transformCanIdToDBCId(recciveMessage[0].toUpperCase());
        pythonModule.callAttr("functionSend",btConnection,recciveMessage[0]);
        if(filtersIsActiv[1]) {
            long idCan = Long.parseLong(id);
            long msk = Long.parseLong(mask,16);
            if((idCan & msk) == msk) {
                String[] data = recciveMessage[1].toUpperCase().split(" ");
                byte[] dataToByte = new byte[data.length];
                try {
                    for (int i = 0; i < dataToByte.length - 1; i++) {
                        dataToByte[i] = (byte) Long.parseLong(data[i], 16);
                    }
                } catch (Exception e) {
                    Log.i("myLog", "Ошибка тут");
                }
                Message messageFromDbc = messagesDBC.get(id);
                if (messageFromDbc == null) {
                    arr.add("message wich id: " + id + "; Not found from DBC file");
                } else {
                    result = result + "Same Name: " + messageFromDbc.getName() + "\n";
                    ArrayList<Signal> signalsMessage = null;
                    signalsMessage = (ArrayList<Signal>) messageFromDbc.getSignals();
                    for (Signal sg : signalsMessage) {
                        String[] dataFromSignal = null;
                        dataFromSignal = DBCParser.extractCANData(dataToByte, sg.getStartBit(), sg.getLength());
                        if (dataFromSignal != null) {
                            String[] dataFromSignalNEW = DBCParser.transformData(dataFromSignal, sg.getByteOrder());
                            result = result + DBCParser.calculationOfValues(dataFromSignalNEW, sg);
                        } else {
                            result = result + sg.getName() + ": Nan" + sg.getUnit() + "\n";
                        }
                    }
                    if (arr.size() == 0) {
                        arr.add(result);
                        positionToNameRatio.put(messageFromDbc.getName(), "" + index);
                    } else {
                        if (positionToNameRatio.get(messageFromDbc.getName()) != null) {
                            int position = Integer.parseInt(positionToNameRatio.get(messageFromDbc.getName()));
                            arr.set(position, result);
                        } else {
                            arr.add(result);
                            index = index + 1;
                            positionToNameRatio.put(messageFromDbc.getName(), "" + index);
                        }
                    }
                    MainActivity main = MyApplication.getInstance().getMainActivity();
                    if (main != null) {
                        metodUpdateList(ad, main);
                    }
                    int hexID = Integer.parseInt(id);
                    String hexNewID = Integer.toHexString(hexID).toUpperCase();
                    messagesCAN.put(messageFromDbc.getName(), new MessageCAN(hexNewID, recciveMessage[1], messageFromDbc.getName(), DBCParser.idConversion(hexNewID)));
                }
            }
        } else{
            String[] data = recciveMessage[1].toUpperCase().split(" ");
            byte[] dataToByte = new byte[data.length];
            try {
                for (int i = 0; i < dataToByte.length - 1; i++) {
                    dataToByte[i] = (byte) Long.parseLong(data[i], 16);
                }
            } catch (Exception e) {
                Log.i("myLog", "Ошибка тут");
            }
            Message messageFromDbc = messagesDBC.get(id);
            if (messageFromDbc == null) {
                arr.add("message wich id: " + id + "; Not found from DBC file");
            } else {
                result = result + "Same Name: " + messageFromDbc.getName() + "\n";
                ArrayList<Signal> signalsMessage = null;
                signalsMessage = (ArrayList<Signal>) messageFromDbc.getSignals();
                for (Signal sg : signalsMessage) {
                    String[] dataFromSignal = null;
                    dataFromSignal = DBCParser.extractCANData(dataToByte, sg.getStartBit(), sg.getLength());
                    if (dataFromSignal != null) {
                        String[] dataFromSignalNEW = DBCParser.transformData(dataFromSignal, sg.getByteOrder());
                        result = result + DBCParser.calculationOfValues(dataFromSignalNEW, sg);
                    } else {
                        result = result + sg.getName() + ": Nan" + sg.getUnit() + "\n";
                    }
                }
                if (arr.size() == 0) {
                    arr.add(result);
                    positionToNameRatio.put(messageFromDbc.getName(), "" + index);
                } else {
                    if (positionToNameRatio.get(messageFromDbc.getName()) != null) {
                        int position = Integer.parseInt(positionToNameRatio.get(messageFromDbc.getName()));
                        arr.set(position, result);
                    } else {
                        arr.add(result);
                        index = index + 1;
                        positionToNameRatio.put(messageFromDbc.getName(), "" + index);
                    }
                }
                MainActivity main = MyApplication.getInstance().getMainActivity();
                if (main != null) {
                    metodUpdateList(ad, main);
                }
                int hexID = Integer.parseInt(id);
                String hexNewID = Integer.toHexString(hexID).toUpperCase();
                messagesCAN.put(messageFromDbc.getName(), new MessageCAN(hexNewID, recciveMessage[1], messageFromDbc.getName(), DBCParser.idConversion(hexNewID)));
            }
        }
    }


    public static void metodUpdateList(MesAdapter adap, Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateList(adap);
            }
        });
    }

    private void showInfo(String name){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Описание");
        builder.setCancelable(true);
        Pattern pattern = Pattern.compile("Same Name: (.+)");
        Matcher matcher = pattern.matcher(name);
        String result = "";
        if (matcher.find()) {
            result = matcher.group(1);
        }
        String param = "";
        MessageCAN message = messagesCAN.get(result);
        if(message != null){
            param = param + "ID: " + message.getId() + "\n";
            param = param + "DATA: " + message.getData() + "\n";
            param = param + message.getIdParametr();
        }
        else{
            param = "error";
        }
        builder.setMessage(param);
        builder.setPositiveButton("Закрыть", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
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
        } else if(item.getItemId() == R.id.id_menu){
            if(btAdapter.isEnabled()) {
                Intent i = new Intent(MainActivity.this, BtListActivity.class);
                startActivity(i);
            }else{
                Toast.makeText(this, "Включите Bluetooth для перехода к bluetooth списку устройств!", Toast.LENGTH_SHORT).show();
            }
        } else if(item.getItemId() == R.id.id_connect){
            synchronized (this) {
                if(btAdapter.isEnabled()) {
                    if (btConnection.getConnectThread() != null && CHC) {
                        btConnection.getConnectThread().closeConnection();
                        progressBar.setVisibility(View.GONE);
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
        else if (item.getItemId() == R.id.id_filter){
            if (statusButtonStart == true) {
                Toast.makeText(MainActivity.this, "Прежде чем настроить фильтр остановите приём данных", Toast.LENGTH_SHORT).show();
            }
            else {
                Intent intent = new Intent(MainActivity.this, FilterActivity.class);
                startActivity(intent);
            }
        }
        else if (item.getItemId() == R.id.id_BSUSAT3){
            Intent intent = new Intent(MainActivity.this, BSUSAT3StartActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void permissionToTransfer(){
        synchronized (this) {
            if(BtConnection.MAC.equals("")){
                Toast.makeText(this, "Выберите bluetooth устройство для работы с ним!", Toast.LENGTH_SHORT).show();
                return;
            }else {
                if (btConnection.getConnectThread() != null) {
                    if (btConnection != null && btConnection.getConnectThread() != null && CHC != false) {
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

    private void setBtIcon(){
        if(btAdapter.isEnabled()){
            menuItem.setIcon(R.drawable.ic_bluetooth_disable);
        }
        else{
            menuItem.setIcon(R.drawable.ic_bluetooth_enable);
        }
    }

    public void setConnectIcon(){
        if(btAdapter.isEnabled() ) {
            if (CHC) {
                menuItemConnectionButton.setIcon(R.drawable.ic_connection_enable);
            } else {
                menuItemConnectionButton.setIcon(R.drawable.ic_connection);
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
        if(requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK){
            uri = data.getData();
            fileDBC = uri.getPath();
            messagesDBC = parser.parse(fileDBC);
            if(fileDBC.equals("default")){
                Toast.makeText(this, "Выбранный вами файл несоответствует формату DBC", Toast.LENGTH_SHORT).show();
                messagesDBC = parser.parse("default");
            }
            else {
                Toast.makeText(this, "Выбранный DBC файл успешно используется", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void init(){
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        pref = getSharedPreferences(BtConsts.MY_PREF,MODE_PRIVATE);
        btConnection = new BtConnection(this);
        if(messages == null) {
            messages = new ArrayList<>();
        }
        ct = messages;
        listView = findViewById(R.id.recive_message_list);
        adapter = new MesAdapter(this,R.layout.message_list_item,messages);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemText = ((TextView) view.findViewById(R.id.text_message)).getText().toString();
                String[] arr = itemText.split("\n");
                showInfo(arr[0]);
            }
        });
        parser = new DBCParser(getApplicationContext());
        if(custom == false) {
            messagesDBC = parser.parse("default");
        }
        if(messagesCAN == null) {
            messagesCAN = new HashMap<>();
        }
        if(positionToNameRatio == null){
            positionToNameRatio = new HashMap<>();
        }
        if(FilterActivity.filterIsActive != null){
            filtersIsActiv = FilterActivity.filterIsActive;
        }
        else{
            filtersIsActiv = new boolean[3];
        }
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(MainActivity.this));
        }
        python = Python.getInstance();
        pythonModule = python.getModule("script");
    }

    @SuppressLint("MissingPermission")
    private void enableBt(){
        Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(i,ENABLE_REQUEST);
    }

    public static MesAdapter getAdapter() {
        return adapter;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putStringArrayList(KEY_RECEIVED_CAN_MESSAGE,messages);
        outState.putSerializable(KEY_MESSAGES_CAN,messagesCAN);
        outState.putSerializable(KEY_POSITION_TO_NAME,positionToNameRatio);
        outState.putInt(KEY_INDEX,index);
        outState.putString(KEY_FILE_DBC,fileDBC);
        outState.putBoolean(KEY_CUSTOM,custom);
        super.onSaveInstanceState(outState);
    }


}