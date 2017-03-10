package com.example.vidish.attendancemanager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.List;

public class ClassSelector extends AppCompatActivity {

    public Spinner spinner;

    SubjectsAsyncTask subjectAsyncTask;

    Bundle mSavedInstanceState;

    String selectedItem="";

    public static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_class_selector);
        setTitle("Welcome "+getIntent().getStringExtra("name"));
        activity=this;
        final RadioGroup radioGroup1 = (RadioGroup) findViewById(R.id.radiogroup_year);
        final RadioGroup radioGroup2 = (RadioGroup) findViewById(R.id.radiogroup_div);
        final RadioGroup radioGroup3 = (RadioGroup) findViewById(R.id.radiogroup_lec);
        final RadioGroup radioGroup4 = (RadioGroup) findViewById(R.id.radiogroup_batch);
        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearlayout);
        for (int i = 0; i < radioGroup2.getChildCount(); i++) {
            (radioGroup2.getChildAt(i)).setEnabled(false);
            (radioGroup3.getChildAt(i)).setEnabled(false);
        }
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setEnabled(false);
        Button proceed = (Button) findViewById(R.id.button_proceed);
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton r1 = (RadioButton) findViewById(checkedId);
                spinner.setAdapter(null);
                spinner.setEnabled(false);
                if (subjectAsyncTask != null)
                    subjectAsyncTask.cancel(true);
                subjectAsyncTask = new SubjectsAsyncTask();
                subjectAsyncTask.execute("http://"+MainActivity.ip+"/getsubjects.php?year=" + r1.getText().toString());
                for (int i = 0; i < radioGroup2.getChildCount(); i++) {
                    (radioGroup2.getChildAt(i)).setEnabled(true);
                    (radioGroup3.getChildAt(i)).setEnabled(true);
                }

            }
        });
        radioGroup3.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton r1= (RadioButton) findViewById(checkedId);
                if(r1.getText().toString().equals("Practical"))
                {
                    linearLayout.setVisibility(View.VISIBLE);
                }
                else if(r1.getText().toString().equals("Lecture"))
                {
                    linearLayout.setVisibility(View.GONE);
                }
            }
        });
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int radioButtonId1 = radioGroup1.getCheckedRadioButtonId();
                int radioButtonId2 = radioGroup2.getCheckedRadioButtonId();
                int radioButtonId3 = radioGroup3.getCheckedRadioButtonId();
                int radioButtonId4 = radioGroup4.getCheckedRadioButtonId();
                final RadioButton year = (RadioButton) findViewById(radioButtonId1);
                final RadioButton div = (RadioButton) findViewById(radioButtonId2);
                final RadioButton lec = (RadioButton) findViewById(radioButtonId3);
                final RadioButton batch = (RadioButton) findViewById(radioButtonId4);
                if (year == null) {
                    Toast.makeText(ClassSelector.this, "Please select a Year", Toast.LENGTH_SHORT).show();
                } else if (div == null) {
                    Toast.makeText(ClassSelector.this, "Please select a Division", Toast.LENGTH_SHORT).show();
                } else if (lec == null) {
                    Toast.makeText(ClassSelector.this, "Please select Lecture/Practical", Toast.LENGTH_SHORT).show();
                } else if(spinner.getAdapter()==null || selectedItem == "" || selectedItem == null ) {
                    Toast.makeText(ClassSelector.this, "Please select a Subject", Toast.LENGTH_SHORT).show();
                } else if(lec.getText().equals("Practical") && batch == null) {
                    Toast.makeText(ClassSelector.this, "Please select a Batch", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ClassSelector.this);
                    alertDialogBuilder.setTitle("ALERT");
                    String messageString = "You're about to take attendance for\nYear: "
                            +"<b>"+ year.getText().toString() +"</b>"+ "<br>Division: " +"<b>"+ div.getText().toString() + "</b>"+
                            "<br>Subject: " +"<b>"+ selectedItem +"</b>"+ "<br>Type: " +"<b>"+ lec.getText().toString() +"</b>";
                    if(lec.getText().toString().equals("Practical"))
                    {
                        messageString += "<br>Batch: "+"<b>"+ batch.getText().toString()+"</b>";
                    }
                    messageString += "<br>Do you wish to proceed?";
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        alertDialogBuilder.setMessage(Html.fromHtml(messageString,0));
                    }
                    else
                    {
                        alertDialogBuilder.setMessage(Html.fromHtml(messageString));
                    }
                    alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(ClassSelector.this, AttendanceManager.class);
                            intent.putExtra("year", year.getText().toString());
                            intent.putExtra("div", div.getText().toString());
                            intent.putExtra("lec", lec.getText().toString());
                            intent.putExtra("subject", selectedItem);
                            if(batch == null || lec.getText().toString().equals("Lecture"))
                                intent.putExtra("batch","");
                            else
                                intent.putExtra("batch", batch.getText().toString());
                            startActivity(intent);
                        }
                    });
                    alertDialogBuilder.setNegativeButton("No", null);
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                }

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_faculty_login; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_class_selector, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.change_password: {
                Intent intent = new Intent(ClassSelector.this,ChangePassword.class)
                        .putExtra("password",getIntent().getStringExtra("password"))
                        .putExtra("id",getIntent().getStringExtra("id"));
                startActivity(intent);
                break;
            }
            case R.id.help: {
                Toast.makeText(ClassSelector.this, "Help", Toast.LENGTH_SHORT).show();

            }
        }
        return super.onOptionsItemSelected(item);
    }

    private class SubjectsAsyncTask extends AsyncTask<String, Void, List<String>> {
        ProgressDialog progressDialog = new ProgressDialog(ClassSelector.this);

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
        protected void onPostExecute(List<String> subjects) {
            if (subjects == null)
                return;

            spinner = (Spinner) findViewById(R.id.spinner);
            spinner.setPrompt("Please select a subject...");
            spinner.setEnabled(true);
            // Creating adapter for spinner
            //ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ClassSelector.this, subjects, R.layout.spinner_layout);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(ClassSelector.this, R.layout.spinner_layout, subjects);
            // Drop down layout style - list view with radio button
            adapter.setDropDownViewResource(R.layout.spinner_layout);

            // attaching data adapter to spinner
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(spinner.getAdapter() != null)
                        selectedItem = parent.getItemAtPosition(position).toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            progressDialog.dismiss();
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
