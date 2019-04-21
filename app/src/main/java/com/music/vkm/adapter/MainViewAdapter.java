package com.music.vkm.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class MainViewAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> fragmentArrayList = new ArrayList<>();
    private ArrayList<String> titleArrayList = new ArrayList<>();

    public MainViewAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return fragmentArrayList.get(i);
    }

    @Override
    public int getCount() {
        return fragmentArrayList.size();
    }

    public void addFragment(Fragment fragment, String title) {
        fragmentArrayList.add(fragment);
        titleArrayList.add(title);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titleArrayList.get(position);
    }
}
