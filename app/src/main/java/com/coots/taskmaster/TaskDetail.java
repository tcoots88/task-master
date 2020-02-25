package com.coots.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;

import java.util.LinkedList;
import java.util.List;

public class TaskDetail extends AppCompatActivity {


    private String TAG = "pvd.taskDetail";
    TaskDatabase taskDatabase;


    List<Task> taskList = new LinkedList<>();


    private AWSAppSyncClient awsAppSyncClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        Log.i(TAG, "Created");
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "Started");
        //connect to local DB
        taskDatabase = Room.databaseBuilder(getApplicationContext(), TaskDatabase.class, "task_database").allowMainThreadQueries().build();

        //connect to AWS
        awsAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        String id = getIntent().getStringExtra("id");
        Log.i(TAG, id + " was clicked");
        Task oneTask = taskDatabase.taskDAO().getOne(id);
        TextView taskTextView = findViewById(R.id.taskTitle);
        taskTextView.setText(oneTask.title);
        TextView descriptionEditText = findViewById(R.id.taskBody);
        descriptionEditText.setText(oneTask.body);
        TextView statusTextView = findViewById(R.id.taskStatus);
        statusTextView.setText(oneTask.state);


//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        taskDatabase = Room.databaseBuilder(getApplicationContext(), TaskDatabase.class, "task_database").allowMainThreadQueries().build();
//
//        Long id = getIntent().getLongExtra("id", 0);
//        Task oneTask = taskDatabase.taskDAO().getOne(id);
//        TextView taskTextView = findViewById(R.id.taskTitle);
//        taskTextView.setText(oneTask.title);
//        TextView desciptTextVeiw = findViewById(R.id.taskBody);
//        desciptTextVeiw.setText(oneTask.body);
//        TextView statusTextView = findViewById(R.id.taskStatus);
//        statusTextView.setText(oneTask.state);
//
//    }
    }
}