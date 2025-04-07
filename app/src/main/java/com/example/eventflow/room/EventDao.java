package com.example.eventflow.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.eventflow.room.LocalEventEntity;


import java.util.List;

@Dao
public interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEvent(LocalEventEntity event);

    @Query("SELECT * FROM events")
    List<LocalEventEntity> getAllEvents();

    @Query("DELETE FROM events")
    void deleteAll();
}
