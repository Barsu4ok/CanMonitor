package com.daniil.canmonitor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.daniil.canmonitor.R;

import java.util.ArrayList;
import java.util.List;

public class MesAdapter extends ArrayAdapter<String> {

    public List<String> getMainList() {
        return mainList;
    }

    private List<String> mainList;

    public MesAdapter(@NonNull Context context, int resource, ArrayList<String> mess) {
        super(context, resource, mess);
        mainList = mess;
    }



    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(mainList.size() != 0) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_item, null, false);
            TextView txtView = (TextView) convertView.findViewById(R.id.text_message);
            txtView.setText(mainList.get(position));
        }
        return convertView;
    }
}
