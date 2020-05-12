package com.example.android.ProTech.Network;

import android.app.Activity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MAC {

    private final static String mRegexMAC = "^%s\\s+0x1\\s+0x2\\s+([:0-9a-fA-F]+)\\s+\\*\\s+\\w+$";
    private final static int mBufferConst = 8 * 1024;
    private WeakReference<Activity> mActivity;

    public MAC(Activity activity) {
    }

    public static String mGetMAC(String ip) {
        String hw = NETDATA.mNoMac;
        BufferedReader bufferedReader = null;
        try {
            if (ip != null) {
                String ptrn = String.format(mRegexMAC, ip.replace(".", "\\."));
                Pattern pattern = Pattern.compile(ptrn);
                bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"), mBufferConst);
                String line;
                Matcher matcher;
                while ((line = bufferedReader.readLine()) != null) {
                    matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        hw = matcher.group(1);
                        break;
                    }
                }
            } else {
            }
        } catch (IOException e) {
            return hw;
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
            }
        }
        return hw;
    }
}