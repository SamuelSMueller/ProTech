package com.example.android.ProTech.Network;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;


public class HOST implements Parcelable {

    public static final int mGateWayType = 0;
    public static final int mComputerType = 1;

    public int mDeviceType = mComputerType;
    public int mAlive = 1;
    public int mPos = 0;
    public int mResponseTime = 0;
    public String mIPAddr = null;
    public String mHostName = null;
    public String mMACAddr = NETDATA.mNoMac;
    public String mVendor = "Unknown";
    public String mOS = "Unknown";
    public HashMap<Integer, String> mServiceMap = null;
    public HashMap<Integer, String> mBannerMap = null;
    public ArrayList<Integer> mOpenPorts = null;
    public ArrayList<Integer> mClosedPorts = null;

    public HOST() {}

    public HOST(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mDeviceType);
        dest.writeInt(mAlive);
        dest.writeString(mIPAddr);
        dest.writeString(mHostName);
        dest.writeString(mMACAddr);
        dest.writeString(mVendor);
        dest.writeString(mOS);
        dest.writeInt(mResponseTime);
        dest.writeInt(mPos);
        dest.writeMap(mServiceMap);
        dest.writeMap(mBannerMap);
        dest.writeList(mOpenPorts);
        dest.writeList(mClosedPorts);
    }

    private void readFromParcel(Parcel in) {
        mDeviceType = in.readInt();
        mAlive = in.readInt();
        mIPAddr = in.readString();
        mHostName = in.readString();
        mMACAddr = in.readString();
        mVendor = in.readString();
        mOS = in.readString();
        mResponseTime = in.readInt();
        mPos = in.readInt();
        mServiceMap = in.readHashMap(null);
        mBannerMap = in.readHashMap(null);
        mOpenPorts = in.readArrayList(Integer.class.getClassLoader());
        mClosedPorts = in.readArrayList(Integer.class.getClassLoader());
    }

    public static final Creator CREATOR = new Creator() {
        public HOST createFromParcel(Parcel in) {
            return new HOST(in);
        }
        public HOST[] newArray(int size) {
            return new HOST[size];
        }
    };
}
