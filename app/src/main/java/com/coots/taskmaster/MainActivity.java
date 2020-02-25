package com.coots.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements MyTaskRecyclerViewAdapter.TaskListener, TaskFragment.OnListFragmentInteractionListener {

    private String TAG= "pvd.main";
    private List<Task> taskList = new LinkedList<>();
    TaskDatabase taskDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taskDatabase = Room.databaseBuilder(getApplicationContext(), TaskDatabase.class, "task_database").allowMainThreadQueries().build();
        this.taskList = taskDatabase.taskDAO().getAll();


        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyTaskRecyclerViewAdapter(this.taskList, this));

        TextView taskTextView = findViewById(R.id.userTask);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String username = sharedPreferences.getString("username", "user");
        if(username == ""){
            username = "user";
        }
        taskTextView.setText(username + "'s tasks.");

        Button addTaskButton = findViewById(R.id.button);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent goToAddTaskIntent = new Intent(MainActivity.this, AddTask.class);
                MainActivity.this.startActivity(goToAddTaskIntent);
            }
        });

        Button allTasksButton = findViewById(R.id.button2);
        allTasksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gotToAllTasksIntent = new Intent(MainActivity.this, AllTasks.class);
                MainActivity.this.startActivity(gotToAllTasksIntent);
            }
        });

        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gotToSettingsIntent = new Intent(MainActivity.this, Settings.class);
                MainActivity.this.startActivity(gotToSettingsIntent);
            }
        });




    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        TextView taskTextView = findViewById(R.id.userTask);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String username = sharedPreferences.getString("username", "user");
        taskTextView.setText(username + "'s tasks.");

    }


    public void sendMessage(View view) {
        Intent intent = new Intent(this, TaskDetail.class);
        TextView title = findViewById(R.id.taskTitle);
        String titleString = title.getText().toString();
        intent.putExtra("task", titleString);
        startActivity(intent);

    }

    @Override
    public void onClickOnTaskCallback(Task task) {
        Log.i(TAG, task.title + "was clicked");
        Intent taskDetailIntent = new Intent(this, TaskDetail.class);
        taskDetailIntent.putExtra("id", task.id);
        MainActivity.this.startActivity(taskDetailIntent);

    }
}
