package com.example.vidish.attendancemanager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Vidish on 24-10-2016.
 */
public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

    public SimpleFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 1)
            return new OverallFragment();
        else if(position == 0)
            return new DailyFragment();
        else
            return new MonthlyFragment();
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        CharSequence title;
        switch(position)
        {
            case 0: title = "Daily";
                break;
            case 1: title = "Overall";
                break;
            case 2: title = "Monthly";
                break;
            default: title = "";
                break;
        }
        return title;
    }
}
