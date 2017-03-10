package com.example.vidish.attendancemanager;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

public class ChangePassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        setTitle("Change Password");
        Button done = (Button) findViewById(R.id.button_done_change_password);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText1,editText2,editText3;
                editText1 = (EditText) findViewById(R.id.edit_text_old_password);
                editText2 = (EditText) findViewById(R.id.edit_text_new_password);
                editText3 = (EditText) findViewById(R.id.edit_text_new_password_confirm);
                if(editText1.getText().toString().equals(getIntent().getStringExtra("password")))
                {
                    if(editText2.getText().toString().length() >=8 && editText3.getText().toString().equals(editText2.getText().toString()))
                    {
                        if(!editText2.getText().toString().contains(" ")) {
                            if(!editText2.getText().toString().contains("&")) {
                                if (!editText1.getText().toString().equals(editText2.getText().toString())) {
                                    ChangePasswordAsyncTask change = new ChangePasswordAsyncTask();
                                    change.execute("http://"+MainActivity.ip+"/changepassword.php?id="
                                    +getIntent().getStringExtra("id")+"&new="+editText2.getText().toString());
                                    Log.v("ChangePassword","http://"+MainActivity.ip+"/changepassword.php?id="
                                            +getIntent().getStringExtra("id")+"&new="+editText2.getText().toString());
                                }
                                else
                                    Toast.makeText(ChangePassword.this, "Current and New Passwords cannot be same", Toast.LENGTH_SHORT).show();
                            }
                            else
                                Toast.makeText(ChangePassword.this, "Ampersand(&) sign not allowed", Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(ChangePassword.this, "Spaces not allowed", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(ChangePassword.this, "New Password doesn't match confirmation", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(ChangePassword.this, "Current Password Incorrect", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class ChangePasswordAsyncTask extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog = new ProgressDialog(ChangePassword.this);

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
            return jsonResponse;
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
        protected void onPostExecute(String jsonResponse) {
            if (jsonResponse.equals("done"))
            {
                progressDialog.dismiss();
                Toast.makeText(ChangePassword.this, "Password has been changed successfully", Toast.LENGTH_SHORT).show();
                ClassSelector.activity.finish();
                finish();
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
