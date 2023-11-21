package com.daniil.canmonitor.utils;
import android.content.Context;
import android.widget.Toast;
public class ThisContext {
    private Context context;

    public ThisContext(Context context) {
        this.context = context;
    }

    public void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
