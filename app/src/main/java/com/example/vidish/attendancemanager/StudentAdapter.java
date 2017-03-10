package com.example.vidish.attendancemanager;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Vidish on 17-09-2016.
 */
public class StudentAdapter extends ArrayAdapter<String> {

    public StudentAdapter(Context context, ArrayList<String> subject) {
        super(context, 0, subject);
    }

    @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Check if an existing view is being reused, otherwise inflate the view
            View listItemView = convertView;
            if (listItemView == null) {
                listItemView = LayoutInflater.from(getContext()).inflate(
                        R.layout.activity_student_attendance_record, parent, false);
            }

        final String currentSubject = getItem(position);

        TextView subjectTextView = (TextView) listItemView.findViewById(R.id.text_view_subject);
        subjectTextView.setText(currentSubject);
        TextView lecture = (TextView) listItemView.findViewById(R.id.text_view_lecture_percentage);
        TextView practical = (TextView) listItemView.findViewById(R.id.text_view_practical_percentage);
        if (OverallFragment.attendanceCountLecture[position][1] != 0) {
            double lecpercentage = Math.floor(OverallFragment.attendanceCountLecture[position][0] / OverallFragment.attendanceCountLecture[position][1] * 10000) / 100;
            lecture.setText(String.format("%.2f%%", lecpercentage));
            if (lecpercentage < 75.0)
                lecture.setBackgroundColor(Color.parseColor("#F44336"));
            else
                lecture.setBackgroundColor(Color.GREEN);
        } else {
            lecture.setText("-");
            lecture.setBackgroundColor(Color.TRANSPARENT);
        }

        if(OverallFragment.attendanceCountPractical[position][1] != 0)
        {
            double pracpercentage = Math.floor(OverallFragment.attendanceCountPractical[position][0] / OverallFragment.attendanceCountPractical[position][1] * 10000) / 100;

            practical.setText(String.format("%.2f%%", pracpercentage));

            if (pracpercentage < 75.0)
                practical.setBackgroundColor(Color.parseColor("#F44336"));
            else
                practical.setBackgroundColor(Color.GREEN);
        } else {
            practical.setText("-");
            practical.setBackgroundColor(Color.TRANSPARENT);
        }
        return listItemView;
    }
}
