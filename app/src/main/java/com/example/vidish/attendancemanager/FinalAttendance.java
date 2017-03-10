package com.example.vidish.attendancemanager;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
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
import java.util.ArrayList;
import java.util.List;

public class FinalAttendance extends AppCompatActivity {

    boolean internetConnectivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle bundle = getIntent().getBundleExtra("bundle");
        setTitle(bundle.getString("year") + " " + bundle.getString("div") + bundle.getString("batch"));
        setContentView(R.layout.roll_no_list);
        Button done = (Button) findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckInternetConnection checkInternet = new CheckInternetConnection();
                try {
                    internetConnectivity = checkInternet.execute(FinalAttendance.this).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (internetConnectivity) {
                    AttendanceAsyncTask attendanceAsyncTask = new AttendanceAsyncTask();
                    attendanceAsyncTask.execute(CreateString(bundle.getString("year"), bundle.getString("div"), bundle.getString("batch"), bundle.getString("subject")));
                }
                else
                    Toast.makeText(FinalAttendance.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
        });
        GridView gridView = (GridView) findViewById(R.id.grid);
        RollNumberAdapter adapter = new RollNumberAdapter(this, AttendanceManager.absent);
        gridView.setAdapter(adapter);
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        ArrayList<RollNumber> absent = new ArrayList<>();
        for (int i = 0; i < RollNumber.count; i++) {
            if (AttendanceManager.rollNumbers.get(i).flag == 0) {
                absent.add(AttendanceManager.rollNumbers.get(i));
            }
        }
        GridView gridView = (GridView) findViewById(R.id.grid);
        RollNumberAdapter adapter = new RollNumberAdapter(this, absent);
        gridView.setAdapter(adapter);
    }

    private String CreateString(String year, String div, String batch, String subject) {
        String link = "http://" + MainActivity.ip + "/attendance.php?table=" + year + "%20" + div +
                "" + batch + "&subject=";
        String a[] = subject.split(" ");
        for (int i = 0; i < a.length; i++) {
            if (i == a.length - 1)
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
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();
            inputStream = urlConnection.getInputStream();
            jsonResponse = readFromStream(inputStream);
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

    private class AttendanceAsyncTask extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog = new ProgressDialog(FinalAttendance.this);

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
            Log.v("FinalAttendance", jsonResponse);
            if (jsonResponse.length() > 1) {
                return "0";
            } else {
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
            if (string.equals("0")) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FinalAttendance.this);
                alertDialogBuilder.setTitle("ALERT");
                alertDialogBuilder.setMessage("Technical Error has occurred. Please try again");
                alertDialogBuilder.setPositiveButton("OK", null);
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                progressDialog.dismiss();
            } else {
                progressDialog.dismiss();
                Intent intent = new Intent(FinalAttendance.this, FacultyLogin.class);
                startActivity(intent);
                AttendanceManager.activity.finish();
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
