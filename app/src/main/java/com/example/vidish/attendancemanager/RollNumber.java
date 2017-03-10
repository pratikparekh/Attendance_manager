package com.example.vidish.attendancemanager;

import android.graphics.Color;

/**
 * Created by Vidish on 17-09-2016.
 */
public class RollNumber {
    public static int count=0;
    private String mrollno;
    public int flag=1;              /*  0=Absent 1=Present  */
    public RollNumber(String rollno)
    {
        mrollno=rollno;
        count++;
    }
    public String getRollNumber()
    {
        return mrollno;
    }
    public void complimentFlag(){
        if (flag==0)
            flag=1;
        else if(flag==1)
            flag=0;
    }
    public boolean getFlag(){
        if(flag == 1)
            return true;
        else
            return false;
    }
    public int getBackgroundColor() {
        if (flag == 1)
            return Color.parseColor("#43A047");
        else
            return Color.RED;
    }
}
