package com.example.android.ProTech.Database;

import android.support.annotation.NonNull;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
    public class Devices {
    //columns
    @PrimaryKey
    @NonNull
    private String MAC;

    @ColumnInfo(name = "isAP")
    private boolean isAccessPoint;

    @ColumnInfo(name = "net")
    private String nSSID;

    @ColumnInfo(name = "oldIsAP")
    private boolean oldAccessPoint;

    @ColumnInfo(name = "oldNet")
    private String oldNSSID;


    //getters
    @NonNull
    public String getMAC() {
        return MAC;
    }

    public boolean isAccessPoint() {
        return isAccessPoint;
    }

    public String getNSSID() {
        return nSSID;
    }

    public boolean isOldAccessPoint() {
        return oldAccessPoint;
    }

    public String getOldNSSID() {
        return oldNSSID;
    }

    //setters
    public void setMAC(String eMAC) {
        this.MAC = eMAC;
    }

    public void setAccessPoint(boolean eAP) {
        this.isAccessPoint = eAP;
    }

    public void setNSSID(String eSSID) {
        this.nSSID = eSSID;
    }

    public void setOldAccessPoint(boolean eAP) {
        this.oldAccessPoint = eAP;
    }

    public void setOldNSSID(String eSSID) {
        this.oldNSSID = eSSID;
    }


    public Devices() {
    }

}
