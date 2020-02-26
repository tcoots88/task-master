package com.coots.taskmaster;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.LinkedList;
import java.util.List;

public class AllTasks extends AppCompatActivity implements MyTaskRecyclerViewAdapter.TaskListener {


    private List<Task> taskList = new LinkedList<>();
    TaskDatabase taskDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_tasks);

        taskDatabase = Room.databaseBuilder(getApplicationContext(), TaskDatabase.class, "task_database").allowMainThreadQueries().build();


    }

    @Override
    protected void onResume() {
        super.onResume();

        this.taskList = taskDatabase.taskDAO().getAll();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyTaskRecyclerViewAdapter(this.taskList,this));


    }

    @Override
    public void onClickOnTaskCallback(Task task) {
        String stringForToast = String.format("%s %s %s", task.title, task.body, task.state);
        Toast saveToast = Toast.makeText(getApplicationContext(), stringForToast, Toast.LENGTH_SHORT);
        saveToast.show();
    }
}
