package com.daniil.canmonitor.adapter;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.Nullable;

public class ListItem {

    private BluetoothDevice btDevice;
    private String itemType = BtAdapter.DEF_ITEM_TYPE;

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public BluetoothDevice getBtDevice() {
        return btDevice;
    }

    @Override
    public int hashCode() {
        return btDevice.getAddress().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if( this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        ListItem item = (ListItem) obj;
        return btDevice.getAddress() != null ? btDevice.getAddress().equals(item.getBtDevice().getAddress()) : item.getBtDevice().getAddress() == null;
    }

    public void setBtDevice(BluetoothDevice btDevice) {
        this.btDevice = btDevice;
    }
}
