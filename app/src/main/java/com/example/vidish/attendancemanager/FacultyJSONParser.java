package com.example.vidish.attendancemanager;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vidish on 09-11-2016.
 */
public class FacultyJSONParser {
    public FacultyJSONParser(){}
    public static List<String> extractFacultyDetails(String json)
    {
        List<String> facultyDetails = new ArrayList();
        try {
            JSONObject root=new JSONObject(json);
            JSONArray result = root.optJSONArray("result");
            if(result.length() == 0 || result == null)
                return null;

            JSONObject object=result.getJSONObject(0);
            String name = object.getString("name");
            String password = object.getString("password");
            Log.v("FacultJSONParser",""+name+" "+password);
            facultyDetails.add(name);
            facultyDetails.add(password);

        } catch (JSONException e) {
            Log.v("JSONParser","exception",e);
        }
        return facultyDetails;
    }
}
