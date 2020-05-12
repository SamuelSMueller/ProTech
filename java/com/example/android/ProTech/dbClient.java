package com.example.android.ProTech;


import android.content.Context;

import android.arch.persistence.room.Room;

import com.example.android.ProTech.Database.LocalDB;

public class dbClient {

        private Context mCtx;
        private static dbClient mInstance;

        private LocalDB appDatabase;

        private dbClient(Context mCtx) {
            this.mCtx = mCtx;

            appDatabase = Room.databaseBuilder(mCtx, LocalDB.class, "NetworkDB").build();
        }

        public static synchronized dbClient getInstance(Context mCtx) {
            if (mInstance == null) {
                mInstance = new dbClient(mCtx);
            }
            return mInstance;
        }

        public LocalDB getAppDatabase() {
            return appDatabase;
        }

}

