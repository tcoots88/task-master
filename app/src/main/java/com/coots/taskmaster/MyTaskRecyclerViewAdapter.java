package com.coots.taskmaster;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.coots.taskmaster.TaskFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Task} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyTaskRecyclerViewAdapter extends RecyclerView.Adapter<MyTaskRecyclerViewAdapter.TasksViewHolder> {

    private final List<Task> tasks;
    private final TaskListener listener;

    public MyTaskRecyclerViewAdapter(List<Task> tasks, TaskListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @Override
    public TasksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_task, parent, false);

        final TasksViewHolder tasksViewHolder = new TasksViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClickOnTaskCallback(tasksViewHolder.task);
            }
        });
        return tasksViewHolder;
    }

    @Override
    public void onBindViewHolder(final TasksViewHolder holder, final int position) {
        holder.task = tasks.get(position);
        holder.taskTitleView.setText(tasks.get(position).title);
//        holder.mBodyView.setText(mValues.get(position).body);
        holder.taskStatusView.setText(tasks.get(position).state);

    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public class TasksViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView taskTitleView;
        //        public final TextView mBodyView;
        public final TextView taskStatusView;
        public Task task;

        public TasksViewHolder(View v) {
            super(v);
            view = v;
            taskTitleView = (TextView) view.findViewById(R.id.taskTitle);
//            mBodyView = (TextView) view.findViewById(R.id.content);
            taskStatusView = (TextView) view.findViewById(R.id.taskState);
        }




        @Override
        public String toString() {
            return super.toString() + " '" + taskTitleView.getText() + "'";
        }

    }

    public  interface TaskListener{
         void onClickOnTaskCallback(Task task);
    }

}