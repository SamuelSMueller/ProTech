package com.example.android.ProTech.Network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NETDATA {
    private static final int mBufferConst = 8 * 1024;
    private static final String mIpCmd = " -f inet addr show %s";
    private static final String mIP1 = "\\s*inet [0-9\\.]+\\/([0-9]+) brd [0-9\\.]+ scope global %s$";
    private static final String mIP2 = "\\s*inet [0-9\\.]+ peer [0-9\\.]+\\/([0-9]+) scope global %s$";
    private static final String mIF = "^%s: packet [0-9\\.]+ mask ([0-9\\.]+) flags.*";
    private static final String mNo = "0";
    public static final String mNoIp = "0.0.0.0";
    public static final String mNoMask = "255.255.255.255";
    public static final String mNoMac = "00:00:00:00:00:00";
    private Context mCtx;
    private WifiInfo mNfo;

    public String mInterface = "eth0";
    public String mIP = mNoIp;
    public int mCIDR = 24;

    public int mSpeed = 0;
    public String mSSID = null;
    public String mBSSID = null;
    public String mCarrier = null;
    public String mMAC = mNoMac;
    public String mMASK = mNoMask;
    public String mGateway = mNoIp;
    public String mAuth = null;

    public NETDATA(final Context mCtx) {
        this.mCtx = mCtx;
        getIp();
        getWifiInfo();
    }

    @Override
    public int hashCode() {
        int ip_custom = 0;
        int ip_start = "0.0.0.0".hashCode();
        int ip_end = "255.255.255.255".hashCode();
        int cidr_custom = 0;
        int cidr = "24".hashCode();
        return 42 + mInterface.hashCode() + mIP.hashCode() + cidr + ip_custom + ip_start + ip_end + cidr_custom + cidr;
    }

    public void getIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                    .hasMoreElements(); ) {
                NetworkInterface ni = en.nextElement();
                mInterface = ni.getName();
                mIP = getInterfaceFirstIp(ni);
                if (mIP != mNoIp) {
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        getCidr();
    }

    private String getInterfaceFirstIp(NetworkInterface ni) {
        if (ni != null) {
            for (Enumeration<InetAddress> nis = ni.getInetAddresses(); nis.hasMoreElements(); ) {
                InetAddress ia = nis.nextElement();
                if (!ia.isLoopbackAddress()) {
                    if (ia instanceof Inet6Address) {

                        continue;
                    }
                    return ia.getHostAddress();
                }
            }
        }
        return mNoIp;
    }

    private void getCidr() {
        if (mMASK != mNoMask) {
            mCIDR = IpToCidr(mMASK);
        } else {
            String match;
            try {
                if ((match = runCommand("/system/xbin/packet", String.format(mIpCmd, mInterface), String.format(mIP1, mInterface))) != null) {
                    mCIDR = Integer.parseInt(match);
                    return;
                } else if ((match = runCommand("/system/xbin/packet", String.format(mIpCmd, mInterface), String.format(mIP2, mInterface))) != null) {
                    mCIDR = Integer.parseInt(match);
                    return;
                } else if ((match = runCommand("/system/bin/ifconfig", " " + mInterface, String.format(mIF, mInterface))) != null) {
                    mCIDR = IpToCidr(match);
                    return;
                } else {

                }
            } catch (NumberFormatException e) {
            }
        }
    }

    private String runCommand(String path, String cmd, String ptn) {
        try {
            if (new File(path).exists() == true) {
                String line;
                Matcher matcher;
                Pattern ptrn = Pattern.compile(ptn);
                Process p = Runtime.getRuntime().exec(path + cmd);
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()), mBufferConst);
                while ((line = r.readLine()) != null) {
                    matcher = ptrn.matcher(line);
                    if (matcher.matches()) {
                        return matcher.group(1);
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public boolean getMobileInfo() {
        TelephonyManager tm = (TelephonyManager) mCtx.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            mCarrier = tm.getNetworkOperatorName();
        }
        return false;
    }

    public boolean getWifiInfo() {
        WifiManager wifi = (WifiManager) mCtx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi != null) {
            mNfo = wifi.getConnectionInfo();
            mSpeed = mNfo.getLinkSpeed();
            mSSID = mNfo.getSSID();
            mBSSID = mNfo.getBSSID();
            mMAC = mNfo.getMacAddress();
            mGateway = getIpFromIntSigned(wifi.getDhcpInfo().gateway);
            mMASK = getIpFromIntSigned(wifi.getDhcpInfo().netmask);
            mAuth = getAuth(wifi);
            return true;
        }
        return false;
    }

    public SupplicantState getSupplicantState() {
        return mNfo.getSupplicantState();
    }

    public static boolean isConnected(Context ctxt) {
        NetworkInfo nfo = ((ConnectivityManager) ctxt
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (nfo != null) {
            return nfo.isConnected();
        }
        return false;
    }

    public static long getUnsignedLongFromIp(String ip_addr) {
        String[] a = ip_addr.split("\\.");
        return (Integer.parseInt(a[0]) * 16777216 + Integer.parseInt(a[1]) * 65536
                + Integer.parseInt(a[2]) * 256 + Integer.parseInt(a[3]));
    }

    public static String getIpFromIntSigned(int ip_int) {
        String ip = "";
        for (int k = 0; k < 4; k++) {
            ip = ip + ((ip_int >> k * 8) & 0xFF) + ".";
        }
        return ip.substring(0, ip.length() - 1);
    }

    public static String getIpFromLongUnsigned(long ip_long) {
        String ip = "";
        for (int k = 3; k > -1; k--) {
            ip = ip + ((ip_long >> k * 8) & 0xFF) + ".";
        }
        return ip.substring(0, ip.length() - 1);
    }

    private int IpToCidr(String ip) {
        double sum = -2;
        String[] part = ip.split("\\.");
        for (String p : part) {
            sum += 256D - Double.parseDouble(p);
        }
        return 32 - (int) (Math.log(sum) / Math.log(2d));
    }


    public String getAuth(WifiManager wifi) {

        List<ScanResult> networkList = wifi.getScanResults();

        String currentBSSID = mNfo.getBSSID();
        String Auth;
        if (networkList != null) {
            for (ScanResult network : networkList) {
                if (currentBSSID.equals(network.BSSID)) {
                    String capabilities =  network.capabilities;

                    if (capabilities.contains("WPA3")) {
                        Auth = "WPA3";
                    } else if (capabilities.contains("WPA2")) {
                        Auth = "WPA2";
                    } else if (capabilities.contains("WPA")) {
                        Auth = "WPA1";
                    } else if (capabilities.contains("WEP")) {
                        Auth = "WEP";
                    }
                    else{
                        Auth = "OPEN";
                    }

                    return Auth;
                }
            }
            Auth = "Error1";
            return Auth;
        }
        else {
            Auth = "Error2";
            return Auth;
        }

    }

}
