package com.coots.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.os.Bundle;
import android.widget.TextView;

public class TaskDetail extends AppCompatActivity {

    TaskDatabase taskDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

    }


    @Override
    protected void onResume() {
        super.onResume();

        taskDatabase = Room.databaseBuilder(getApplicationContext(), TaskDatabase.class, "task_database").allowMainThreadQueries().build();

        Long id = getIntent().getLongExtra("id", 0);
        Task oneTask = taskDatabase.taskDAO().getOne(id);
        TextView taskTextVeiw = findViewById(R.id.taskTitle);
        taskTextVeiw.setText(oneTask.title);
        TextView desciptTextVeiw = findViewById(R.id.taskBody);
        desciptTextVeiw.setText(oneTask.body);
        TextView statusTextVeiw = findViewById(R.id.taskStatus);
        statusTextVeiw.setText(oneTask.state);

    }
}