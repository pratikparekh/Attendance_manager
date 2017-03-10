package com.example.vidish.attendancemanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Vidish on 25-10-2016.
 */
public class MonthlyFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    View fragmentLayout;
    String monthName;
    int mp;
    String link;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentLayout = inflater.inflate(R.layout.monthpicker, container, false);
        Spinner month = (Spinner) fragmentLayout.findViewById(R.id.spinner_monthpicker_monthpicker);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("MM");
        date.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
        String localTime = date.format(currentLocalTime);

        String[] monthArray = new String[]{"July","August","September","October","November"};
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getContext(), R.layout.spinner_layout, monthArray);
        adapter2.setDropDownViewResource(R.layout.spinner_layout);
        month.setOnItemSelectedListener(this);
        month.setAdapter(adapter2);
        final int monthno = Integer.parseInt(localTime)-7;
        month.setSelection(monthno);
        Button done = (Button) fragmentLayout.findViewById(R.id.button_done_month);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mp > monthno )
                    Toast.makeText(getActivity(), "Error in Month", Toast.LENGTH_SHORT).show();
                else {
                    link = "http://" + MainActivity.ip + "/month.php?id=" + getActivity().getIntent().getStringExtra("id") +
                            "&year=" + OverallFragment.year[0] + "%20" + OverallFragment.year[1] + "&batch=" + OverallFragment.batch[0] + "%20" + OverallFragment.batch[1];
                    link = link + "&month=";
                    if (mp + 7 < 10) {
                        link = link + "0" + (mp + 7);
                    } else {
                        link = link + (mp + 7);
                    }
                    OverallFragment.attendanceCountLecture = new float[OverallFragment.subjects.size()][2];
                    OverallFragment.attendanceCountPractical = new float[OverallFragment.subjects.size()][2];
                    Intent intent = new Intent(getActivity(),MonthlyActivity.class).putExtra("link",link);
                    startActivity(intent);
                }
            }
        });
        return fragmentLayout;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        monthName = parent.getItemAtPosition(position).toString();
        mp = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
