package com.coots.taskmaster;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;


@Dao
public abstract class TaskDAO {
    @Query("SELECT * FROM task ORDER BY id DESC")
    abstract List<Task> getAll();

    @Query("SELECT * FROM task WHERE id= :id")
    abstract Task getOne(long id);

    @Insert
    abstract void save(Task task);

}
