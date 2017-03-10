package com.example.vidish.attendancemanager;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class AttendanceManager extends AppCompatActivity {


    public static ArrayList<RollNumber> rollNumbers, absent;
    boolean internetConnectivity;

    public static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.roll_no_list);
        final Bundle bundle = getIntent().getExtras();
        setTitle(bundle.getString("year") + " " + bundle.getString("div")+bundle.getString("batch"));

        Button done = (Button) findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (absent.isEmpty() || absent.size() == RollNumber.count) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AttendanceManager.this);
                    alertDialogBuilder.setTitle("ALERT");
                    if (absent.size() == RollNumber.count)
                        alertDialogBuilder.setMessage("Are all student Absent?");
                    else
                        alertDialogBuilder.setMessage("Are all students present?");
                    alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CheckInternetConnection checkInternet = new CheckInternetConnection();
                            try{
                                internetConnectivity = checkInternet.execute(AttendanceManager.this).get();
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                            if(internetConnectivity) {
                                AttendanceAsyncTask attendanceAsyncTask = new AttendanceAsyncTask();
                                attendanceAsyncTask.execute(CreateString(bundle.getString("year"), bundle.getString("div"), bundle.getString("batch"), bundle.getString("subject")));
                            }
                        }
                    });
                    alertDialogBuilder.setNegativeButton("No",null);
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else {
                    Intent intent = new Intent(AttendanceManager.this, FinalAttendance.class);
                    intent.putExtra("bundle", bundle);
                    startActivity(intent);
                }
            }
        });

        rollNumbers = new ArrayList<>();
        absent = new ArrayList<>();

        SAPIDAsyncTask sapidAsyncTask = new SAPIDAsyncTask();
        sapidAsyncTask.execute("http://"+MainActivity.ip+"/dbconnect.php?table=" + bundle.getString("year") + "%20" + bundle.getString("div") +
                                bundle.getString("batch")+"&json=1");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        RollNumber.count = 0;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        GridView gridView = (GridView) findViewById(R.id.grid);

        RollNumberAdapter adapter = new RollNumberAdapter(AttendanceManager.this, rollNumbers);

        gridView.setAdapter(adapter);
        activity = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_faculty_login; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_attendance_manager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.reset_attendance:
                for (int i = 0; i < RollNumber.count; i++) {
                    rollNumbers.get(i).flag = 1;
                }
                absent.clear();
                break;
            case R.id.invert:
                for (int i = 0; i < RollNumber.count; i++) {
                    if (rollNumbers.get(i).flag == 1) {
                        absent.add(rollNumbers.get(i));
                        rollNumbers.get(i).flag = 0;
                    } else if (rollNumbers.get(i).flag == 0) {
                        absent.remove(rollNumbers.get(i));
                        rollNumbers.get(i).flag = 1;
                    }
                }
                break;
            case R.id.all_absent:
                for (int i = 0; i < RollNumber.count; i++) {
                    rollNumbers.get(i).flag = 0;
                    absent.add(rollNumbers.get(i));
                }
                break;
        }
        GridView gridView = (GridView) findViewById(R.id.grid);

        RollNumberAdapter adapter = new RollNumberAdapter(AttendanceManager.this, rollNumbers);

        gridView.setAdapter(adapter);
        return super.onOptionsItemSelected(item);
    }

    private class SAPIDAsyncTask extends AsyncTask<String, Void, Void> {
        ProgressDialog progressDialog = new ProgressDialog(AttendanceManager.this);

        @Override
        protected Void doInBackground(String... urls) {
            publishProgress();
            if (urls.length < 1 || urls[0] == null)
                return null;
            URL url;
            try {
                url = new URL(urls[0]);
            } catch (MalformedURLException exception) {
                Log.e("ClassSelector", "Error with creating URL", exception);
                return null;
            }
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
            }
            if (jsonResponse == null) {
                return null;
            }
            rollNumbers = JSONParser.extractRollNumbers(jsonResponse);

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setIndeterminate(true);
            progressDialog.setTitle("Loading");
            progressDialog.setMessage("Please Wait");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void v) {
            progressDialog.dismiss();
            if (rollNumbers == null)
                return;
            final GridView gridView = (GridView) findViewById(R.id.grid);

            RollNumberAdapter adapter = new RollNumberAdapter(AttendanceManager.this, rollNumbers);

            gridView.setAdapter(adapter);
        }

    }

    private String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        if (url == null) {
            return jsonResponse;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(15000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();
            //if (urlConnection.getResponseCode() == 200) {
            inputStream = urlConnection.getInputStream();
            jsonResponse = readFromStream(inputStream);
            //}
        } catch (IOException e) {
            jsonResponse = null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // function must handle java.io.IOException here
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }




    private String CreateString(String year,String div,String batch,String subject)
    {
        String link = "http://"+MainActivity.ip+"/attendance.php?table="+year+"%20"+div+
                ""+batch+"&subject=";
        String a[]=subject.split(" ");
        for(int i = 0; i<a.length;i++)
        {
            if(i == a.length-1)
                link = link + a[i];
            else
                link = link + a[i] + "%20";
        }
        link = link + "&record=";
        for (int i = 0; i < AttendanceManager.rollNumbers.size(); i++) {
            if (AttendanceManager.rollNumbers.get(i).getFlag()) {
                link = link + "P";
            } else {
                link = link + "A";
            }
        }
        return link;
    }
    private class AttendanceAsyncTask extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog = new ProgressDialog(AttendanceManager.this);

        @Override
        protected String doInBackground(String... urls) {
            publishProgress();
            if (urls.length < 1 || urls[0] == null)
                return null;
            URL url;
            try {
                url = new URL(urls[0]);
            } catch (MalformedURLException exception) {
                Log.e("ClassSelector", "Error with creating URL", exception);
                return null;
            }
            String jsonResponse = "";
            publishProgress();
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
            }
            if (jsonResponse == null) {
                return null;
            }
            Log.v("FinalAttendance",jsonResponse);
            if(jsonResponse.length()>1)
            {
                return "0";
            }
            else {
                return jsonResponse;
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setIndeterminate(true);
            progressDialog.setTitle("Loading");
            progressDialog.setMessage("Please Wait");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String string) {
            if(string.equals("0"))
            {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AttendanceManager.this);
                alertDialogBuilder.setTitle("ALERT");
                alertDialogBuilder.setMessage("Technical Error has occurred. Please try again");
                alertDialogBuilder.setPositiveButton("OK", null);
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                progressDialog.dismiss();
            }
            else {
                progressDialog.dismiss();
                Intent intent = new Intent(AttendanceManager.this, FacultyLogin.class);
                startActivity(intent);
                if (ClassSelector.activity != null)
                    ClassSelector.activity.finish();
                if (FacultyLogin.activity != null)
                    FacultyLogin.activity.finish();
                RollNumber.count = 0;
                finish();
            }
        }

    }

}
