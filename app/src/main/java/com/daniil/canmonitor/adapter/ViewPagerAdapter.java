package com.daniil.canmonitor.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.daniil.canmonitor.BSUSAT3.fragments.PowerManagerFragment;
import com.daniil.canmonitor.BSUSAT3.fragments.Test;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private static final int NUM_PAGES = 6;
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PowerManagerFragment();
            case 1:
                return new Test();
            case 2:
                return new Test();
            case 3:
                return new Test();
            case 4:
                return new Test();
            case 5:
                return new Test();
            default:
                return new Test();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}
