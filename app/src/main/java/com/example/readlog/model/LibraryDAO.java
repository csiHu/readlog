package com.example.readlog.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LibraryDAO {
    @Insert
    void insertLibraryItem(LibraryItem libraryItem);

    @Query("SELECT * FROM Library WHERE status = :status ORDER BY title COLLATE NOCASE ASC")
    LiveData<List<LibraryItem>> getItemsByStatus(String status);

    @Update
    void updateLibraryItem(LibraryItem libraryItem);

    @Delete
    void deleteLibraryItem(LibraryItem libraryItem);
}
