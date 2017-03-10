package com.example.vidish.attendancemanager;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

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

public class MonthlyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_list);
        AttendanceAsyncTask attendanceAsyncTask = new AttendanceAsyncTask();
        attendanceAsyncTask.execute(getIntent().getStringExtra("link"));
    }

    private class AttendanceAsyncTask extends AsyncTask<String, Void, List> {

        ProgressDialog progressDialog = new ProgressDialog(MonthlyActivity.this);
        @Override
        protected List doInBackground(String... urls) {
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
            List<AttendanceObject> attendanceObjectList = AttendanceRecordJSONParser.extractAttendanceRecord(jsonResponse);
            return attendanceObjectList;
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
        protected void onPostExecute(List list) {
            progressDialog.dismiss();
            ArrayList<AttendanceObject> attendanceObjectArrayList = ((ArrayList<AttendanceObject>) list);
            if(attendanceObjectArrayList == null)
            {}
            else {
                for (int i = 0; i < attendanceObjectArrayList.size(); i++) {
                    if (attendanceObjectArrayList.get(i).getType().equals("Lecture")) {
                        if (attendanceObjectArrayList.get(i).getAttendance().equals("P")) {
                            OverallFragment.attendanceCountLecture[OverallFragment.subjects.indexOf(attendanceObjectArrayList.get(i).getSubject())][0] += 1;
                        }
                        OverallFragment.attendanceCountLecture[OverallFragment.subjects.indexOf(attendanceObjectArrayList.get(i).getSubject())][1] += 1;
                    } else if (attendanceObjectArrayList.get(i).getType().equals("Practical")) {
                        if (attendanceObjectArrayList.get(i).getAttendance().equals("P")) {
                            OverallFragment.attendanceCountPractical[OverallFragment.subjects.indexOf(attendanceObjectArrayList.get(i).getSubject())][0] += 1;
                        }
                        OverallFragment.attendanceCountPractical[OverallFragment.subjects.indexOf(attendanceObjectArrayList.get(i).getSubject())][1] += 1;
                    }
                }
            }


            StudentAdapter adapter = new StudentAdapter(MonthlyActivity.this, OverallFragment.subjects);

            ListView listView = (ListView) findViewById(R.id.list);

            listView.setAdapter(adapter);
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
            urlConnection.setReadTimeout(10000 /* milliseconds */);
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
}
