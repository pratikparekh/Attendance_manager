package com.example.vidish.attendancemanager;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vidish on 22-10-2016.
 */
public class AttendanceRecordJSONParser {
    public AttendanceRecordJSONParser()
    {}
    public static List<AttendanceObject> extractAttendanceRecord(String json)
    {
        List<AttendanceObject> attendanceList = new ArrayList<>();
        try {
            JSONObject root=new JSONObject(json);
            JSONArray result = root.optJSONArray("result");
            if(result.length() == 0 || result == null)
                return null;
            for(int i=0;i<result.length();i++)
            {
                JSONObject object=result.getJSONObject(i);
                String date=object.getString("date");
                String subject=object.getString("subject");
                String attendance=object.getString("attendance");
                String type=object.getString("type");
                attendanceList.add(new AttendanceObject(date,subject,attendance,type));
            }

        } catch (JSONException e) {
            Log.v("JSONParser","exception",e);
        }
        return attendanceList;
    }
}
