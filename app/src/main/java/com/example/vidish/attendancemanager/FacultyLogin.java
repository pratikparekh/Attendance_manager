package com.example.vidish.attendancemanager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.example.vidish.attendancemanager.CheckInternetConnection.*;

public class FacultyLogin extends AppCompatActivity {

    Button button, login;
    EditText editText, password;
    String scanContent;
    FacultyLoginAsyncTask facultyLoginAsyncTask;
    boolean internetConnectivity;
    public static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_login);
        setTitle("Faculty Login");
        activity = this;

        SharedPreferences sharedPref = getSharedPreferences("mypref", 0);
        scanContent = sharedPref.getString("facultyId", null);

        button = (Button) findViewById(R.id.button_faculty_scan_barcode);
        editText = (EditText) findViewById(R.id.edit_text_faculty_id);
        login = (Button) findViewById(R.id.faculty_login);
        password = (EditText) findViewById(R.id.edit_text_password);

        if (scanContent != null) {
            editText.setText(scanContent);
            button.setText("Change ID");
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(FacultyLogin.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
                integrator.setPrompt("Scan the barcode on your ID");
                integrator.initiateScan();
                editText.setEnabled(false);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckInternetConnection checkInternet = new CheckInternetConnection();
                try{
                    internetConnectivity = checkInternet.execute(FacultyLogin.this).get();
                }catch (Exception e) {
                    e.printStackTrace();
                }
                if (internetConnectivity) {
                    if (editText.getText().toString().length() != 0 && password.getText().toString().length() >= 8) {
                        facultyLoginAsyncTask = new FacultyLoginAsyncTask();
                        facultyLoginAsyncTask.execute("http://" + MainActivity.ip + "/facultylogin.php?id="
                                + editText.getText().toString());
                        Log.v("FacultyLogin LINK", "http://" + MainActivity.ip + "/facultylogin.php?id="
                                + editText.getText().toString());
                    } else {
                        if (editText.getText().toString().length() == 0)
                            Toast.makeText(FacultyLogin.this, "Please enter your ID", Toast.LENGTH_SHORT).show();
                        else if (password.getText().toString().length() == 0)
                            Toast.makeText(FacultyLogin.this, "Please enter your Password", Toast.LENGTH_SHORT).show();
                        else if (password.getText().toString().length() < 8)
                            Toast.makeText(FacultyLogin.this, "Password needs to be Minimum 8 characters", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(FacultyLogin.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    View view = FacultyLogin.this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        if (editText.getText().toString().length()!=0 && password.getText().toString().length() != 0) {

                            CheckInternetConnection checkInternet = new CheckInternetConnection();
                            try{
                                internetConnectivity = checkInternet.execute(FacultyLogin.this).get();
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                            if(internetConnectivity) {
                                facultyLoginAsyncTask = new FacultyLoginAsyncTask();
                                facultyLoginAsyncTask.execute("http://" + MainActivity.ip + "/facultylogin.php?id="
                                        + editText.getText().toString());
                            }
                            else
                            {
                                Toast.makeText(FacultyLogin.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            if(editText.getText().toString().length()==0)
                                Toast.makeText(FacultyLogin.this, "Please enter your ID", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(FacultyLogin.this, "Please enter your Password", Toast.LENGTH_SHORT).show();Toast.makeText(FacultyLogin.this, "Please enter your ID.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (scanningResult != null) {

            scanContent = scanningResult.getContents();
            editText.setText(scanContent);

            if (scanContent == null)
                button.setText("Scan Barcode");

            else
                button.setText("Change ID");

            SharedPreferences settings = getSharedPreferences("mypref", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("facultyId", scanContent);
            editor.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_faculty_login; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_faculty_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.refresh: {
                EditText e = (EditText) findViewById(R.id.edit_text_faculty_id);
                e.setText("");
                SharedPreferences settings = getSharedPreferences("mypref", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("facultyId", null);
                editor.commit();
                e = (EditText) findViewById(R.id.edit_text_password);
                e.setText(null);
                e.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(e.getWindowToken(), 0);
                break;
            }
            case R.id.help: {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Help");
                alertDialogBuilder.setMessage("Step 1: Press the Scan Barcode(or Change ID) button to scan your ID(or change the " +
                        "existing ID).\nStep 2: Scan your barcode."
                        + "\nStep 3: Enter your Password and press Login.");
                alertDialogBuilder.setPositiveButton("Ok", null);
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private class FacultyLoginAsyncTask extends AsyncTask<String, Void, List<String>> {
        ProgressDialog progressDialog = new ProgressDialog(FacultyLogin.this);

        @Override
        protected List<String> doInBackground(String... urls) {
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
            List<String> subjects = FacultyJSONParser.extractFacultyDetails(jsonResponse);
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
        protected void onPostExecute(List<String> facultyDetails) {
            progressDialog.dismiss();
            if(facultyDetails == null)
            {
                Toast.makeText(FacultyLogin.this, "Couldn't retreive data. Please try again", Toast.LENGTH_SHORT).show();
                return;
            }

            if(facultyDetails.get(1).equals(password.getText().toString()))
            {
                SharedPreferences settings = getSharedPreferences("mypref", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("facultyId", editText.getText().toString());
                editor.commit();
                Intent intent = new Intent(FacultyLogin.this,ClassSelector.class)
                        .putExtra("name", facultyDetails.get(0))
                        .putExtra("password",facultyDetails.get(1))
                        .putExtra("id",editText.getText().toString());
                startActivity(intent);
            }
            else
            {
                Toast.makeText(FacultyLogin.this, "Incorrect Details. Please Try Again", Toast.LENGTH_SHORT).show();
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
    }
}
