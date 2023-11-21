package com.daniil.canmonitor.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.daniil.canmonitor.BSUSAT3.Module;
import com.daniil.canmonitor.BSUSAT3.fragments.PowerManagerFragment;
import com.daniil.canmonitor.R;

import java.util.ArrayList;

public class PowerManagerItemAdapter extends ArrayAdapter<Module> {
    public static final double MAX_VOLTAGE = 277;
    public static final double MAX_CURRENT = 500;
    private final Context context;
    private final ArrayList<Module> elements;

    public PowerManagerItemAdapter(Context context, ArrayList<Module> elements) {
        super(context, R.layout.module_item, elements);
        this.context = context;
        this.elements = elements;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.module_item, parent, false);
        }
        TextView itemName = convertView.findViewById(R.id.item_name);
        Switch switchElement = convertView.findViewById(R.id.btn_start_stop);
        switchElement.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                elements.get(position).setIsActive(true);
            } else {
                elements.get(position).setIsActive(false);
            }
            });
        TextView voltageValue = convertView.findViewById(R.id.voltage_value);
        TextView currentValue = convertView.findViewById(R.id.current_value);

        Module module = elements.get(position);
        itemName.setText(module.getName());
        switchElement.setChecked(module.getisActive());
        voltageValue.setText(module.getVoltage() + "V");
        currentValue.setText(module.getCurrent() + "A");

        if(module.getisActive()) {
            if (module.getVoltage() > MAX_VOLTAGE || module.getCurrent() > MAX_CURRENT) {
                convertView.setBackgroundColor(Color.RED);
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT); // Или любой другой цвет по умолчанию
            }
        }

        return convertView;
    }
}
