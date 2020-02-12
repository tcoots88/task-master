package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button addTask = findViewById(R.id.button);
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent addTaskIntent = new Intent(MainActivity.this, AddTask.class);
                MainActivity.this.startActivity(addTaskIntent);
            }
        });




        Button allTaskButton = findViewById(R.id.button2);
        allTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent allTasksIntent = new Intent(MainActivity.this, AllTasks.class);
                MainActivity.this.startActivity(allTasksIntent);
            }
        });
    }


}