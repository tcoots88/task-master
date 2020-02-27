package com.coots.taskmaster;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.annotation.Nonnull;

import type.CreateTaskInput;


public class AddTask extends AppCompatActivity {

    private String TAG = "pvd.addTask";


    TaskDatabase taskDatabase;
    //AWS Database
    private AWSAppSyncClient awsAppSyncClient;

    ImageView file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        taskDatabase = Room.databaseBuilder(getApplicationContext(), TaskDatabase.class, "task_database")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries().build();

        awsAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        Button attachFileButton = findViewById(R.id.attachFileButton);
        file = findViewById(R.id.imageUpload);
        attachFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent grabFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                grabFileIntent.setType("file/*");
                startActivity(grabFileIntent);
            }
        });



        Button submitButton = findViewById(R.id.button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText titleEditText = findViewById(R.id.titleEditText);
                String newTitle = titleEditText.getText().toString();
                EditText descriptionEditText = findViewById(R.id.descriptionEditText);
                String newDescription = descriptionEditText.getText().toString();


                addOneTaskToDynamoDB(newTitle, newDescription);

                //Toasts
                Toast submitToast = Toast.makeText(getApplicationContext(), "Submitted!", Toast.LENGTH_SHORT);
                submitToast.show();
//                Intent gotToMainActivityIntent = new Intent(AddTask.this, MainActivity.class);
//                AddTask.this.startActivity(gotToMainActivityIntent);

            }
        });
    }

    public void addOneTaskToDynamoDB(String title, String body){
        CreateTaskInput createTaskInput = CreateTaskInput.builder().
                title(title).
                body(body).
                state("New").
                build();

        awsAppSyncClient.mutate(CreateTaskMutation.builder().input(createTaskInput).build())
                .enqueue(mutationCallback);
    }

    private GraphQLCall.Callback<CreateTaskMutation.Data> mutationCallback = new GraphQLCall.Callback<CreateTaskMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateTaskMutation.Data> response) {
            Log.i(TAG, "Added Task");
            String dynamoDBID = response.data().createTask().id();
            System.out.println("dynamoDBID = " + dynamoDBID);
            String title = response.data().createTask().title();
            String body = response.data().createTask().body();
            Task newTask = new Task(title,body, "New", dynamoDBID);
            taskDatabase.taskDAO().save(newTask);
            Intent gotToMainActivityIntent = new Intent(AddTask.this, MainActivity.class);
            AddTask.this.startActivity(gotToMainActivityIntent);
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());
        }
 };
    public void uploadWithTransferUtility() {

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                        .build();

        File file = new File(getApplicationContext().getFilesDir(), "sample.txt");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.append("Howdy World!");
            writer.close();
        }
        catch(Exception e) {
            Log.e(TAG, e.getMessage());
        }

        TransferObserver uploadObserver =
                transferUtility.upload(
                        "public/sample.txt",
                        new File(getApplicationContext().getFilesDir(),"sample.txt"));

        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d(TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
            }

        });

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.
        if (TransferState.COMPLETED == uploadObserver.getState()) {
            // Handle a completed upload.
        }

        Log.d(TAG, "Bytes Transferred: " + uploadObserver.getBytesTransferred());
        Log.d(TAG, "Bytes Total: " + uploadObserver.getBytesTotal());
    }

}
