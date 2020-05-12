package com.example.android.ProTech.Database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

//Database Object
@Entity
public class Networks {
    //columns
    @PrimaryKey
    @NonNull
    private String SSID;

    @ColumnInfo(name = "BSSID")
    @NonNull
    private String BSSID;

    @ColumnInfo(name = "oldBSSID")
    private String oldBSSID;

    @ColumnInfo(name = "Security")
    private String security;

    @ColumnInfo(name = "oldSecurity")
    private String oldSecurity;

    @ColumnInfo(name = "isPublic")
    private boolean isPublic;

    @ColumnInfo(name = "oldIsPublic")
    private boolean oldIsPublic;

    @ColumnInfo(name = "NumDev")
    private int numDev;

    @ColumnInfo(name = "oldNumDev")
    private int oldNumDev;

    @ColumnInfo(name = "ConnectionCount")
    private int conCount;

    @ColumnInfo(name = "oldConnectionCount")
    private int oldConCount;

    @ColumnInfo(name = "ConnectionSpeed")
    private int conSpeed;

    @ColumnInfo(name = "oldConnectionSpeed")
    private int oldConSpeed;

    //getters
    @NonNull
    public String getSSID() {
        return SSID;
    }
    public String getBSSID(){ return BSSID; }
    public String getOldBSSID(){ return oldBSSID; }
    public String getSecurity() {
        return security;
    }
    public String getOldSecurity() { return oldSecurity; }
    public boolean getPublic() {
        return isPublic;
    }
    public boolean isOldIsPublic() { return oldIsPublic; }
    public int getNumDev() { return numDev; }
    public int getOldNumDev() {return oldNumDev; }
    public int getConCount() { return conCount; }
    public int getOldConCount() {return oldConCount; }
    public int getConSpeed() { return conSpeed; }
    public int getOldConSpeed() {return oldConSpeed; }


    //setters
    public void setSSID(String eSSID){
        this.SSID = eSSID;
    }

    public void setBSSID(String eBSSID){
        this.BSSID = eBSSID;
    }
    public void setSecurity(String eSecurity){
        this.security = eSecurity;
    }
    public void setPublic(boolean ePublic){
        this.isPublic = ePublic;
    }
    public void setNumDev(int eNumDev){
        this.numDev = eNumDev;
    }
    public void setConCount(int eConCount) { this.conCount = eConCount; }
    public void setConSpeed(int eConSpeed) { this.conSpeed = eConSpeed; }

    public void setOldBSSID(String eOldBSSID){
        this.oldBSSID = eOldBSSID;
    }
    public void setOldSecurity(String eSecurity){
        this.oldSecurity = eSecurity;
    }
    public void setOldIsPublic(boolean oldIsPublic) { this.oldIsPublic = oldIsPublic; }
    public void setOldNumDev(int eNumDev){
        this.oldNumDev = eNumDev;
    }
    public void setOldConCount(int eConCount) { this.oldConCount = eConCount; }
    public void setOldConSpeed(int eConSpeed) { this.oldConSpeed = eConSpeed; }


    public Networks(){}

}
