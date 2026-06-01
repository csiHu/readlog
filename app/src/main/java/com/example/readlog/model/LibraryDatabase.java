package com.example.readlog.model;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = LibraryItem.class, version = 1, exportSchema = false)
public abstract class LibraryDatabase extends RoomDatabase {
    public abstract LibraryDAO libraryDAO();
}
