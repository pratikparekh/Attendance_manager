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

public class DailyFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    View fragmentLayout;
    String monthName;
    String dayName;
    int dp,mp;
    String link;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        fragmentLayout = inflater.inflate(R.layout.datepicker, container, false);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("MM-dd");
        date.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));

        final String localTime[] = date.format(currentLocalTime).split("-");
        Spinner day = (Spinner) fragmentLayout.findViewById(R.id.spinner_daypicker);
        Spinner month = (Spinner) fragmentLayout.findViewById(R.id.spinner_monthpicker);

        String[] monthArray = new String[]{"July","August","September","October","November","December"};
        String[] dayArray = new String[]{"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20",
        "21","22","23","24","25","26","27","28","29","30","31"};

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getContext(), R.layout.spinner_layout, monthArray);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getContext(), R.layout.spinner_layout, dayArray);

        adapter1.setDropDownViewResource(R.layout.spinner_layout);
        adapter2.setDropDownViewResource(R.layout.spinner_layout);
        day.setOnItemSelectedListener(this);
        month.setOnItemSelectedListener(this);

        day.setAdapter(adapter1);
        month.setAdapter(adapter2);
        final int dayno = Integer.parseInt(localTime[1])-1;
        final int monthno = Integer.parseInt(localTime[0])-7;
        day.setSelection(dayno);
        month.setSelection(monthno);

        Button done = (Button) fragmentLayout.findViewById(R.id.button_done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("DailyFragment","MP="+mp+" DP="+dp+" DAYNO="+dayno+" MONTHNO="+monthno);
                if(((mp == 2 || mp == 4) && dp == 31) || (dp>dayno && mp>=monthno) || (dp<dayno && mp>monthno))
                {
                    Toast.makeText(getActivity(), "Error in Date", Toast.LENGTH_SHORT).show();
                }
                else {
                    link = "http://" + MainActivity.ip + "/month.php?id=" + getActivity().getIntent().getStringExtra("id") +
                            "&year=" + OverallFragment.year[0] + "%20" + OverallFragment.year[1] + "&batch=" + OverallFragment.batch[0] + "%20" + OverallFragment.batch[1];
                    link = link + "&month=";
                    if (mp + 7 < 10) {
                        link = link + "0" + (mp + 7) + "-";
                    } else {
                        link = link + (mp + 7) + "-";
                    }
                    if (dp + 1 < 10) {
                        link = link + "0" + (dp + 1);
                    } else {
                        link = link + (dp + 1);
                    }
                    Log.v("Link", link);
                    OverallFragment.attendanceCountLecture = new float[OverallFragment.subjects.size()][2];
                    OverallFragment.attendanceCountPractical = new float[OverallFragment.subjects.size()][2];
                    Intent intent = new Intent(getContext(),DailyActivity.class).putExtra("link",link);
                    startActivity(intent);
                }
            }
        });
        return fragmentLayout;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        if(spinner.getId() == R.id.spinner_daypicker)
        {
            dayName = parent.getItemAtPosition(position).toString();
            dp=position;
        }
        else if(spinner.getId() == R.id.spinner_monthpicker)
        {
            monthName = parent.getItemAtPosition(position).toString();
            mp=position;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

}
