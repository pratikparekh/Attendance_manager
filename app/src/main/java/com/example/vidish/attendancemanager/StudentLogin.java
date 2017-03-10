package com.example.vidish.attendancemanager;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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

public class StudentLogin extends AppCompatActivity {

    Button login;
    Button button;
    EditText editText;
    String scanContent;
    NameAsyncTask nameAsyncTask;
    boolean internetConnectivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login);
        button = (Button) findViewById(R.id.button_student_scan_barcode);
        editText = (EditText) findViewById(R.id.edit_text_student_id);
        nameAsyncTask = new NameAsyncTask();
        SharedPreferences sharedPref = getSharedPreferences("mypref", 0);
        scanContent = sharedPref.getString("studentId", null);
        int direct = getIntent().getIntExtra("direct",1);
        if (direct == 1) {
            CheckInternetConnection checkInternet = new CheckInternetConnection();
            try {
                internetConnectivity = checkInternet.execute(StudentLogin.this).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (internetConnectivity) {
                CancelNotification(StudentLogin.this,0);
                nameAsyncTask = new NameAsyncTask();
                nameAsyncTask.execute("http://" + MainActivity.ip + "/studentlogin.php?id=" + scanContent);
            }
            else
                Toast.makeText(StudentLogin.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
        } else {
            setTitle("Student Login");

            if (scanContent != null) {
                editText.setText(scanContent);
                button.setText("Change ID");
            }

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentIntegrator integrator = new IntentIntegrator(StudentLogin.this);
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
                    integrator.setPrompt("Scan the barcode on your ID");
                    integrator.initiateScan();
                }
            });

            login = (Button) findViewById(R.id.student_login);
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scanContent = editText.getText().toString().trim();
                    if (editText.getText().toString().equals("")) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(StudentLogin.this);
                        alertDialogBuilder.setTitle("ALERT");
                        alertDialogBuilder.setMessage("Please Scan your ID");
                        alertDialogBuilder.setPositiveButton("Ok", null);
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    } else {
                        CheckInternetConnection checkInternet = new CheckInternetConnection();
                        try {
                            internetConnectivity = checkInternet.execute(StudentLogin.this).get();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (internetConnectivity){
                            if (nameAsyncTask.getStatus() == AsyncTask.Status.RUNNING)
                                nameAsyncTask.cancel(true);
                        nameAsyncTask = new NameAsyncTask();
                        nameAsyncTask.execute("http://" + MainActivity.ip + "/studentlogin.php?id=" + editText.getText().toString());
                    }
                        else
                            Toast.makeText(StudentLogin.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        editText.setText(scanContent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (scanningResult != null) {

            scanContent = scanningResult.getContents();
            editText.setText(scanContent);

            if (scanContent == null)
                button.setText("Scan Barcode");

            else {
                button.setText("Change ID");
                CheckInternetConnection checkInternet = new CheckInternetConnection();
                try{
                    internetConnectivity = checkInternet.execute(StudentLogin.this).get();
                }catch (Exception e) {
                    e.printStackTrace();
                }
                if(internetConnectivity) {
                    if (nameAsyncTask.getStatus() == AsyncTask.Status.RUNNING)
                        nameAsyncTask.cancel(true);
                    nameAsyncTask = new NameAsyncTask();
                    nameAsyncTask.execute("http://" + MainActivity.ip + "/studentlogin.php?id=" + scanContent);
                }
            }
        }
    }

    public static void CancelNotification(Context ctx, int notifyId) {
        String  s = Context.NOTIFICATION_SERVICE;
        NotificationManager mNM = (NotificationManager) ctx.getSystemService(s);
        mNM.cancel(notifyId);
    }

    private class NameAsyncTask extends AsyncTask<String, Void, List> {
        ProgressDialog progressDialog = new ProgressDialog(StudentLogin.this);

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
            List<String> subjects = SubjectJSONParser.extractSubjects(jsonResponse);
            return subjects;
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
        protected void onPostExecute(List subject) {
            progressDialog.dismiss();
            if (subject == null || subject.size() == 0)
                Toast.makeText(getBaseContext(), "Wrong SAP", Toast.LENGTH_SHORT).show();
            else {
                ArrayList<String> arrayList = (ArrayList<String>) subject;
                SharedPreferences settings = getSharedPreferences("mypref", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("studentId", scanContent);
                editor.apply();
                Intent i = new Intent(StudentLogin.this, ViewPager.class);
                i.putStringArrayListExtra("subject", arrayList);
                i.putExtra("id", scanContent);
                startActivity(i);
            }
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
