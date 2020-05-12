package com.example.android.ProTech.Network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RATE {

    private static final int mBuffer = 512;
    private final int mTimeout = 5000;
    private final String mCMD = "/system/bin/ping -A -q -n -w 3 -W 2 -c 3 ";
    private final String mRegex = "^rtt min\\/avg\\/max\\/mdev = [0-9\\.]+\\/[0-9\\.]+\\/([0-9\\.]+)\\/[0-9\\.]+ ms.*";
    private Pattern mPattern;
    private String mLine;
    public String mInd = null;
    public int mRate = 800;

    public RATE() {
        mPattern = Pattern.compile(mRegex);
    }

    public void adaptRate() {
        int response_time = 0;
        if ((response_time = getAvgResponseTime(mInd)) > 0) {
            if (response_time > 100) {
                mRate = response_time * 5;
            } else {
                mRate = response_time * 10;
            }
            if (mRate > mTimeout) {
                mRate = mTimeout;
            }
        }
    }

    private int getAvgResponseTime(String host) {
        BufferedReader reader = null;
        Matcher matcher;
        try {
            final Process proc = Runtime.getRuntime().exec(mCMD + host);
            reader = new BufferedReader(new InputStreamReader(proc.getInputStream()), mBuffer);
            while ((mLine = reader.readLine()) != null) {
                matcher = mPattern.matcher(mLine);
                if (matcher.matches()) {
                    reader.close();
                    return (int) Float.parseFloat(matcher.group(1));
                }
            }
            reader.close();
        } catch (Exception e) {
            try {
                final long start = System.nanoTime();
                if (InetAddress.getByName(host).isReachable(mTimeout)) {
                    return (int) ((System.nanoTime() - start) / 1000);
                }
            } catch (Exception e1) {
            }
        } finally {
            try {
            if (reader != null) {
                reader.close();
            }
            } catch(IOException e){
            }
        }
        return mRate;
    }
}
