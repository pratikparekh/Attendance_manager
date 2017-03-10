package com.example.vidish.attendancemanager;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Vidish on 17-09-2016.
 */
public class RollNumberAdapter extends ArrayAdapter<RollNumber> {
    public RollNumberAdapter(Context context, ArrayList<RollNumber> rollNumber) {
        super(context, 0, rollNumber);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        View gridItemView = convertView;
        if (gridItemView == null) {
            gridItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.activity_attendance_manager, parent, false);

        }
        final RollNumber currentRollNo = getItem(position);
        final TextView rollnoTextView = (TextView) gridItemView.findViewById(R.id.text_view_roll_no);
        rollnoTextView.setText(currentRollNo.getRollNumber());
        rollnoTextView.setBackgroundColor(currentRollNo.getBackgroundColor());
        gridItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentRollNo.complimentFlag();
                rollnoTextView.setBackgroundColor(currentRollNo.getBackgroundColor());
                if(!currentRollNo.getFlag())
                {
                    AttendanceManager.absent.add(currentRollNo);
                }
                else {
                    AttendanceManager.absent.remove(currentRollNo);
                }
            }
        });
        return gridItemView;
    }
}
