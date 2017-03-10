package com.example.vidish.attendancemanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ViewPager extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);
        try {
            getSupportActionBar().setElevation(0);
        }catch (NullPointerException e)
        {}


        android.support.v4.view.ViewPager viewPager = (android.support.v4.view.ViewPager) findViewById(R.id.viewpager);

        // Create an adapter that knows which fragment should be shown on each page
        SimpleFragmentPagerAdapter adapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager());

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        viewPager.setCurrentItem(1);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
