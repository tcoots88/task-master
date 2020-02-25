package com.coots.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddTask extends AppCompatActivity {

    TaskDatabase taskDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        taskDatabase = Room.databaseBuilder(getApplicationContext(), TaskDatabase.class, "task_database").allowMainThreadQueries().build();


        Button submitButton = findViewById(R.id.button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText titleEditText = findViewById(R.id.titleEditText);
                String newTitle = titleEditText.getText().toString();
                EditText descriptionEditText = findViewById(R.id.desciptEditText);
                String newDescription = descriptionEditText.getText().toString();

                Task newTask = new Task(newTitle,newDescription, "New");
                taskDatabase.taskDAO().save(newTask);

                //Toasts
                Toast submitToast = Toast.makeText(getApplicationContext(), "Submitted!", Toast.LENGTH_SHORT);
                submitToast.show();
                Intent gotToMainActivityIntent = new Intent(AddTask.this, MainActivity.class);
                AddTask.this.startActivity(gotToMainActivityIntent);

            }
        });
    }
}