package com.example.vidish.attendancemanager;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Vidish on 25-09-2016.
 */
public class JSONParser {
    public JSONParser(){}
    public static ArrayList<RollNumber> extractRollNumbers(String json)
    {
        ArrayList<RollNumber> rollNumbers=new ArrayList<>();
        try {
            JSONObject root=new JSONObject(json);
            JSONArray result=root.optJSONArray("result");
            for(int i=0;i<result.length();i++)
            {
                JSONObject object=result.getJSONObject(i);
                String col=object.getString("col");
                if(col.charAt(0)=='6') {
                    rollNumbers.add(new RollNumber(col));
                }
            }
        } catch (JSONException e) {
            Log.v("JSONParser","exception",e);
        }
        return rollNumbers;
    }
}
