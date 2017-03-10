package com.example.vidish.attendancemanager;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
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

/**
 * Created by Vidish on 24-10-2016.
 */
public class OverallFragment extends Fragment {

    static ArrayList<String> subjects;
    static float[][] attendanceCountLecture,attendanceCountPractical;
    View fragmentLayout;
    AttendanceAsyncTask attendanceAsyncTask;
    static String[] batch,year;
    boolean isViewShown = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        fragmentLayout = inflater.inflate(R.layout.subject_list, container, false);

        subjects = getActivity().getIntent().getStringArrayListExtra("subject");

        if(subjects == null)
            Toast.makeText(getActivity(), "NULL", Toast.LENGTH_SHORT).show();
        if(subjects.get(0).equals("Applied Mathematics III")||subjects.get(0).equals("Business Communication and Ethics")
                ||subjects.get(0).equals("Artificial Intelligence")||subjects.get(0).equals("null"))
        {
            getActivity().setTitle("Welcome "+getActivity().getIntent().getStringExtra("id"));
        }
        else {
            getActivity().setTitle("Welcome " + subjects.get(0));
            subjects.remove(0);
        }
        String y = subjects.get(subjects.size()-1);
        subjects.remove(subjects.size()-1);
        year = y.split(" ");
        String b = subjects.get(subjects.size()-1);
        subjects.remove(subjects.size()-1);
        batch = b.split(" ");
        attendanceCountLecture = new float[subjects.size()][2];
        attendanceCountPractical= new float[subjects.size()][2];


        attendanceAsyncTask = new AttendanceAsyncTask();
        if(!isViewShown) {
        attendanceAsyncTask.execute("http://"+MainActivity.ip+"/getattendance.php?id="+getActivity().getIntent().getStringExtra("id")+
                "&year="+year[0]+"%20"+year[1]+"&batch="+batch[0]+"%20"+batch[1]);
        }

        return fragmentLayout;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getView() != null) {
            isViewShown = true;
            if (attendanceAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING) || attendanceAsyncTask.getStatus().equals(AsyncTask.Status.PENDING)) {
                attendanceAsyncTask.cancel(true);
            }
        }
        else
        {
            isViewShown = false;
        }
    }

    private class AttendanceAsyncTask extends AsyncTask<String, Void, List> {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());

        @Override
        protected List doInBackground(String... urls) {
            //publishProgress();
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
            ArrayList<AttendanceObject> attendanceObjectArrayList = ((ArrayList<AttendanceObject>) list);

            //progressDialog.dismiss();
            if (attendanceObjectArrayList == null)
                return;
            for(int i=0;i<attendanceObjectArrayList.size();i++)
            {
                if(attendanceObjectArrayList.get(i).getType().equals("Lecture"))
                {
                    if(attendanceObjectArrayList.get(i).getAttendance().equals("P")) {
                        attendanceCountLecture[subjects.indexOf(attendanceObjectArrayList.get(i).getSubject())][0] += 1;
                    }
                    attendanceCountLecture[subjects.indexOf(attendanceObjectArrayList.get(i).getSubject())][1] += 1;
                }
                else if (attendanceObjectArrayList.get(i).getType().equals("Practical"))
                {
                    if(attendanceObjectArrayList.get(i).getAttendance().equals("P")) {
                        attendanceCountPractical[subjects.indexOf(attendanceObjectArrayList.get(i).getSubject())][0] += 1;
                    }
                    attendanceCountPractical[subjects.indexOf(attendanceObjectArrayList.get(i).getSubject())][1] += 1;
                }
            }

            StudentAdapter adapter = new StudentAdapter(getActivity(), subjects);

            ListView listView = (ListView) fragmentLayout.findViewById(R.id.list);

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
