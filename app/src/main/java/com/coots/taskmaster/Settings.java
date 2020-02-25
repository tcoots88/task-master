package com.coots.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class Settings extends AppCompatActivity {

    private static final String TAG = "pvd.settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "User saved username");
                TextInputEditText usernameTextInput = findViewById(R.id.username);
                String username = usernameTextInput.getText().toString();
                System.out.println(username);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", username);
                editor.apply();
                Toast saveToast = Toast.makeText(getApplicationContext(), "Saved!", Toast.LENGTH_SHORT);
                saveToast.show();
                Intent goHomeIntent = new Intent(Settings.this, MainActivity.class);
                Settings.this.startActivity(goHomeIntent);

            }
        });


    }
}