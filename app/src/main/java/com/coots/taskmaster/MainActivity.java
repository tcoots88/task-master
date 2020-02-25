package com.coots.taskmaster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignOutOptions;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.exception.ApolloException;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import static com.amazonaws.mobile.client.UserState.SIGNED_IN;
import static com.amazonaws.mobile.client.UserState.SIGNED_OUT;


public class MainActivity extends Activity implements MyTaskRecyclerViewAdapter.TaskListener {


        private String TAG= "pvd.main";
        private List<Task> taskList = new LinkedList<>();
        TaskDatabase taskDatabase;

        private AWSAppSyncClient awsAppSyncClient;

        @SuppressLint("SetTextI18n")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {

                        @Override
                        public void onResult(final UserStateDetails userStateDetails) {
                            Log.i("INIT", "onResult: " + userStateDetails.getUserState());
                            if(userStateDetails.getUserState() == SIGNED_OUT){
                                // 'this' refers the the current active activity
                                AWSMobileClient.getInstance().showSignIn(MainActivity.this, new Callback<UserStateDetails>() {
                                    @Override
                                    public void onResult(UserStateDetails result) {
                                        Log.d(TAG, "onResult: " + result.getUserState());
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Log.e(TAG, "onError: ", e);
                                    }
                                });
                            }

                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("INIT", "Initialization error.", e);
                        }
                    }
            );

            taskDatabase = Room.databaseBuilder(getApplicationContext(), TaskDatabase.class, "task_database").allowMainThreadQueries().build();

            //connect to AWS
            awsAppSyncClient = AWSAppSyncClient.builder()
                    .context(getApplicationContext())
                    .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                    .build();


            //call method to retrieve tasks from AWS
            taskList = new LinkedList<>();
            getAllTasksFromDynamoDB();

            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new MyTaskRecyclerViewAdapter(this.taskList, MainActivity.this));

            TextView taskTextView = findViewById(R.id.userTask);
            String username = AWSMobileClient.getInstance().getUsername();
            System.out.println(username);

            taskTextView.setText(username + "'s tasks.");

            Button signOutButton = findViewById(R.id.signOut);
            signOutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AWSMobileClient.getInstance().signOut(SignOutOptions.builder().signOutGlobally(true).build(), new Callback<Void>() {
                        @Override
                        public void onResult(final Void result) {
                            Log.d(TAG, "signed-out");
                            Intent mainIntent = new Intent(MainActivity.this, MainActivity.class);
                            MainActivity.this.startActivity(mainIntent);
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "sign-out error", e);
                        }
                    });
                }
            });

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

        @SuppressLint("SetTextI18n")
        @Override
        protected void onResume() {
            super.onResume();
            TextView taskTextView = findViewById(R.id.userTask);
            String username = AWSMobileClient.getInstance().getUsername();
            System.out.println("username = " + username);

            taskTextView.setText(username + "'s tasks.");
            getAllTasksFromDynamoDB();

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
            Log.i(TAG, task.title + " was clicked");
            Intent taskDetailIntent = new Intent(this, TaskDetail.class);
            Log.i(TAG, task.dynamoDBId + " was clicked");
            taskDetailIntent.putExtra("id", task.dynamoDBId);
            MainActivity.this.startActivity(taskDetailIntent);

        }

        public void getAllTasksFromDynamoDB(){
            awsAppSyncClient.query(ListTasksQuery.builder().build())
                    .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                    .enqueue(todosCallback);
        }

        private GraphQLCall.Callback<ListTasksQuery.Data> todosCallback = new GraphQLCall.Callback<ListTasksQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<ListTasksQuery.Data> response) {
                Log.i(TAG, response.data().listTasks().items().toString());
                if (taskList.size() == 0 || response.data().listTasks().items().size() != taskList.size()) {
                    taskList.clear();
                    for (ListTasksQuery.Item item : response.data().listTasks().items()) {
                        Task retrievedTask = new Task(item.title(), item.body(), item.state(), item.id());
                        taskList.add(retrievedTask);
                    }
                    Handler handlerForMainThread = new Handler(Looper.getMainLooper()) {
                        @Override
                        public void handleMessage(@NonNull Message msg) {
                            super.handleMessage(msg);
                            RecyclerView recyclerView = findViewById(R.id.recyclerView);
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }
                    };

                    handlerForMainThread.obtainMessage().sendToTarget();

                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, e.toString());
            }
        };
}