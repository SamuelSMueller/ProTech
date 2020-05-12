package com.example.android.ProTech.Database;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.List;

public class NetworkWithDevices {
        @Embedded
        public Networks network;
        @Relation(
                parentColumn = "SSID",
                entityColumn = "net"
        )
        public List<Devices> devices;
    }
