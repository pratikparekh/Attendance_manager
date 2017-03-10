package com.example.vidish.attendancemanager;

public class AttendanceObject {
    private String mdate,msubject,mattendance,mtype;
    public AttendanceObject(String date,String subject,String attendance,String type)
    {
        mdate=date;
        msubject=subject;
        mattendance=attendance;
        mtype=type;
    }
    public String getDate()
    {
        return mdate;
    }
    public String getSubject()
    {
        return msubject;
    }
    public String getAttendance()
    {
        return mattendance;
    }
    public String getType()
    {
        return mtype;
    }
}
