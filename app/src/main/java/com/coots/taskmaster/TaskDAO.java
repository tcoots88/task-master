package com.coots.taskmaster;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;


@Dao
public interface TaskDAO {

    @Query("SELECT * FROM task ORDER BY id DESC")
    List<Task> getAll();

    @Query("SELECT * FROM task WHERE dynamoDBId = :dynamoDBId")
    Task getOne(String dynamoDBId);

    @Insert
    void save(Task task);

    @Delete
    void delete(Task task);
}
