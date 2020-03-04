package com.coots.taskmaster;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.UUID;

import javax.annotation.Nonnull;

import type.CreateTaskInput;


public class AddTask extends AppCompatActivity {

    private String TAG = "pvd.addTask";

    TaskDatabase taskDatabase;
    private AWSAppSyncClient awsAppSyncClient;

    ImageView file;
    String uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        taskDatabase = Room.databaseBuilder(getApplicationContext(), TaskDatabase.class, "task_database").allowMainThreadQueries().build();

        awsAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        file = findViewById(R.id.imageUpload);

        Intent intent = getIntent();

        String intentType = intent.getType();
        if (intentType != null && intentType.contains("image/")) {
            // Handle intents with image data ...
            Uri imageURI = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if(imageURI != null) {
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
                } else {
                    Log.i(TAG + " imageURI", imageURI.toString());
                    onImagePicked(imageURI);
                }
            }
        }

    }
    public void submit(View v) {

        EditText titleEditText = findViewById(R.id.titleEditText);
        String newTitle = titleEditText.getText().toString();
        System.out.println("newtitle" +newTitle);
        EditText descriptionEditText = findViewById(R.id.descriptionEditText);
        String newDescription = descriptionEditText.getText().toString();


        addOneTaskToDynamoDB(newTitle, newDescription, "public/" + uuid);

        //Toasts
        Toast submitToast = Toast.makeText(getApplicationContext(), "Submitted!", Toast.LENGTH_SHORT);
        submitToast.show();
//                Intent gotToMainActivityIntent = new Intent(AddTask.this, MainActivity.class);
//                AddTask.this.startActivity(gotToMainActivityIntent);

    }

    public void addOneTaskToDynamoDB(String title, String body, String uri) {
        CreateTaskInput createTaskInput = CreateTaskInput.builder().
                title(title).
                body(body).
                state("New").
                uri(uri).
                build();

        awsAppSyncClient.mutate(CreateTaskMutation.builder().input(createTaskInput).build())
                .enqueue(mutationCallback);
    }

    private GraphQLCall.Callback<CreateTaskMutation.Data> mutationCallback = new GraphQLCall.Callback<CreateTaskMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateTaskMutation.Data> response) {
            Log.i(TAG, "Added Task");
            Handler handler = new Handler(Looper.getMainLooper());
            String dynamoDBID = response.data().createTask().id();
            System.out.println("dynamoDBID = " + dynamoDBID);
            String title = response.data().createTask().title();
            String body = response.data().createTask().body();
            String uri = response.data().createTask().uri();
            Task newTask = new Task(title, body, "New", dynamoDBID, uri);
            taskDatabase.taskDAO().save(newTask);
            Intent gotToMainActivityIntent = new Intent(AddTask.this, MainActivity.class);
            AddTask.this.startActivity(gotToMainActivityIntent);
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());
        }
    };

    public void uploadWithTransferUtility(Uri uri) {

        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri,
                filePathColumn, null, null, null);
        assert cursor != null;
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance(), Region.getRegion(Regions.US_WEST_2)))
                        .build();

        uuid = UUID.randomUUID().toString();
        TransferObserver uploadObserver =
                transferUtility.upload(
                        "taskmaster879d0b8cad184fecbff2609a8bf14c8c210659-todo",
                        "public/" + uuid,
                        new File(picturePath));

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
                int percentDone = (int) percentDonef;

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

    @SuppressLint("IntentReset")
    public void addImage(View v){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        } else {
            Intent grabFileIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            grabFileIntent.setType("image/*");
//                grabFileIntent.putExtra(DocumentsContract.EXTRA_INITIAL_URI,);
            startActivityForResult(grabFileIntent, 999);
        }
    }

    @SuppressLint("IntentReset")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode != 0) {
            return;
        }
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent grabFileIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            grabFileIntent.setType("image/*");
//                grabFileIntent.putExtra(DocumentsContract.EXTRA_INITIAL_URI,);
            startActivityForResult(grabFileIntent, 999);
        }
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 999) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Get the URI that points to the selected contact
                Uri imageURI = intent.getData();
                Log.i(TAG + " imageURI",imageURI.toString());
                onImagePicked(imageURI);
            }
        }
    }

    public void onImagePicked(Uri uri){
        file.setImageURI(uri);
        uploadWithTransferUtility(uri);
    }
}
