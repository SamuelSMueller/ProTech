package com.example.android.ProTech.Database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import java.util.List;

//Data Access Object
    @Dao
    public interface NetworkDao {

        @Transaction
        @Query("SELECT * FROM Networks")
        public List<NetworkWithDevices> getNetworksDB();

        @Query("SELECT * FROM Devices")
        public List<Devices> getDevicesDB();

        @Query("SELECT * FROM Networks WHERE SSID IN (:SSIDs)")
        public List<Networks> getNetworkDB(String[] SSIDs);

        @Query("SELECT * FROM Devices WHERE MAC IN (:MACs)")
        public List<Devices> getDeviceDB(String[] MACs);

        @Query("SELECT * FROM Devices WHERE isAP = 1 AND net IN (:SSIDs)")
        public List<Devices> getAPDB(String[] SSIDs);

        @Query("SELECT * FROM Devices WHERE net IN (:SSIDs)")
        public List<Devices> getNetDevicesDB(String[] SSIDs);

        //single packet
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertNetwork(Networks networks);

        //multiple devices
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertDevices(Devices... device);

        //single device
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertDevice(Devices device);

        //single packet with devices
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertAll(Networks network, List<Devices> devices);

        @Update(onConflict = OnConflictStrategy.REPLACE)
        void updateAll(Networks network, List<Devices> devices);

        @Update
        void updateNet(Networks network);

        @Update
        void updateDevice(Devices device);

        @Update
        void updateDevices(List<Devices> devices);

        @Delete
        void deleteNetwork(Networks network);

        @Delete
        void deleteDevice(Devices device);

        @Delete
        void deleteDevices(Devices... devices);

        @Delete
        void deleteAll(Networks network, List<Devices> devices);
    }
