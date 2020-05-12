package com.example.android.ProTech.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;


//App database
    @Database(entities = {Networks.class, Devices.class}, version = 1, exportSchema = false)
    public abstract class LocalDB extends RoomDatabase {
        public abstract NetworkDao networkDao();
        }


