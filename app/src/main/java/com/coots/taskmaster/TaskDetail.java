package com.coots.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class TaskDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

    }

    @Override
    protected void onStart() {
        super.onStart();
        TextView taskTitle = findViewById(R.id.taskTitle);
        String newTask = getIntent().getStringExtra("task");
        taskTitle.setText(newTask);
    }
}
