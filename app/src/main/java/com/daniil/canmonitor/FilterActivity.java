package com.daniil.canmonitor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.daniil.canmonitor.adapter.BtAdapter;
import com.daniil.canmonitor.adapter.ListItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class FilterActivity extends AppCompatActivity {

    public static String KEY_MAP_FILTERS = "KEY_MAP_FILTERS";
    private Button buttonApply;
    private EditText minIdEditText;
    private EditText maxIdEditText;
    private EditText maskEditText;
    private RadioGroup radioGroup;
    private RadioButton radioButtonRule1;
    private RadioButton radioButtonRule2;
    private CheckBox checkBoxFilterRange;
    private CheckBox checkBoxFilterMask;
    public static boolean[]  filterIsActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        getSupportActionBar().setTitle("Filter Settings");
        if(savedInstanceState != null){
            filterIsActive = savedInstanceState.getBooleanArray(KEY_MAP_FILTERS);
        }
        init();
        minIdEditText.setText(MainActivity.minCanId);
        maxIdEditText.setText(MainActivity.maxCanId);
        maskEditText.setText(MainActivity.mask);
        setRadioButton();
        buttonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    long min  = Long.parseLong(minIdEditText.getText().toString(),16);
                    long max  = Long.parseLong(maxIdEditText.getText().toString(),16);
                    long mask = Long.parseLong(maskEditText.getText().toString(),16);
                    MainActivity.minCanId = minIdEditText.getText().toString().toUpperCase();
                    MainActivity.maxCanId = maxIdEditText.getText().toString().toUpperCase();
                    MainActivity.mask = maskEditText.getText().toString().toUpperCase();
                    Toast.makeText(FilterActivity.this, "Настройки применены", Toast.LENGTH_SHORT).show();
                    minIdEditText.setText(minIdEditText.getText().toString().toUpperCase());
                    maxIdEditText.setText(maxIdEditText.getText().toString().toUpperCase());
                    maskEditText.setText(maskEditText.getText().toString().toUpperCase());
                }
                catch (Exception e ){
                    Toast.makeText(FilterActivity.this,"Введены некорректные значения",Toast.LENGTH_SHORT).show();
                }
            }
        });
        checkBoxFilterRange.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    filterIsActive[0] = true;
                }
                else filterIsActive[0] = false;
            }
        });
        checkBoxFilterMask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    filterIsActive[1] = true;
                }
                else filterIsActive[1] = false;
            }
        });

    }
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.radioButton1:
                if (checked) {
                    MainActivity.takeInboxes = true;
                    radioButtonRule2.setChecked(false);
                    filterIsActive[2] = true;
                }
                break;
            case R.id.radioButton2:
                if (checked) {
                    MainActivity.takeInboxes = false;
                    radioButtonRule1.setChecked(false);
                    filterIsActive[2] = false;
                }
                break;
        }
    }
    @SuppressLint("MissingPermission")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    private void init(){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar == null) return;
        actionBar.setDisplayHomeAsUpEnabled(true);
        buttonApply = findViewById(R.id.buttonApply);
        minIdEditText = findViewById(R.id.editTextMinId);
        maxIdEditText = findViewById(R.id.editTextMaxId);
        maskEditText = findViewById(R.id.editTextMask);
        radioGroup = findViewById(R.id.radioGroup);
        radioButtonRule1 = findViewById(R.id.radioButton1);
        radioButtonRule2 = findViewById(R.id.radioButton2);
        checkBoxFilterRange = findViewById(R.id.filterRange);
        checkBoxFilterMask = findViewById(R.id.filterMask);
        if (filterIsActive == null) {
            filterIsActive = new boolean[3];
            filterIsActive[2] = true;
        }
        if (filterIsActive != null) {
            setCheckBox();
        }
    }

    public void setRadioButton(){
        if(MainActivity.takeInboxes){
            radioButtonRule1.setChecked(true);
        } else radioButtonRule2.setChecked(true);
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBooleanArray(KEY_MAP_FILTERS,filterIsActive);
        super.onSaveInstanceState(outState);
    }
    public void setCheckBox(){
        checkBoxFilterRange.setChecked(filterIsActive[0]);
        checkBoxFilterMask.setChecked(filterIsActive[1]);
    }

}