package com.daniil.canmonitor.BSUSAT3.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.daniil.canmonitor.BSUSAT3.Module;
import com.daniil.canmonitor.R;
import com.daniil.canmonitor.adapter.PowerManagerItemAdapter;

import java.util.ArrayList;


public class PowerManagerFragment extends Fragment {

    public static ArrayList<Module> elements;

    public PowerManagerFragment() {
        init();
    }
    private void init(){
        if (elements == null) {
            elements = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                elements.add(new Module("Slot " + (i + 1), false, 0, 0));
            }
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_power_manager, container, false);
        ListView listView = view.findViewById(R.id.list_view_power_manager);

        PowerManagerItemAdapter adapter = new PowerManagerItemAdapter(requireContext(), elements);
        listView.setAdapter(adapter);
        return view;
    }
}