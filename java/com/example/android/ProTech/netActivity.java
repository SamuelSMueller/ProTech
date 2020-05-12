package com.example.android.ProTech;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.example.android.ProTech.Network.NETDATA;

public abstract class netActivity extends Activity {

    private ConnectivityManager mConnMgr;
    protected Context mCtx;
    protected SharedPreferences mPref = null;
    protected NETDATA mNET = null;
    protected String mIPStr = "";
    protected String mINStr = "";
    protected String mMOStr = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCtx = getApplicationContext();
        mPref = PreferenceManager.getDefaultSharedPreferences(mCtx);
        mConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mNET = new NETDATA(mCtx);

    }

    @Override
    public void onResume() {
        super.onResume();
        setButtons(true);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    protected abstract void setInfo();

    protected abstract void setButtons(boolean disable);

    protected abstract void cancelTasks();

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("StringFormatMatches")
        public void onReceive(Context ctxt, Intent intent) {
            mIPStr = "";
            mMOStr = "";

            String action = intent.getAction();
            if (action != null) {
                if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                    int WifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                    switch (WifiState) {
                        case WifiManager.WIFI_STATE_ENABLING:
                            mINStr = getString(R.string.wifi_enabling);
                            break;
                        case WifiManager.WIFI_STATE_ENABLED:
                            mINStr = getString(R.string.wifi_enabled);
                            break;
                        case WifiManager.WIFI_STATE_DISABLING:
                            mINStr = getString(R.string.wifi_disabling);
                            break;
                        case WifiManager.WIFI_STATE_DISABLED:
                            mINStr = getString(R.string.wifi_disabled);
                            break;
                        default:
                            mINStr = getString(R.string.wifi_unknown);
                    }
                }

                if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION) && mNET.getWifiInfo()) {
                    SupplicantState sstate = mNET.getSupplicantState();
                    if (sstate == SupplicantState.SCANNING) {
                        mINStr = getString(R.string.wifi_scanning);
                    } else if (sstate == SupplicantState.ASSOCIATING) {
                        mINStr = getString(R.string.wifi_associating,
                                (mNET.mSSID != null ? mNET.mSSID : (mNET.mBSSID != null ? mNET.mBSSID
                                        : mNET.mMAC)));
                    } else if (sstate == SupplicantState.COMPLETED) {
                        mINStr = getString(R.string.wifi_dhcp, mNET.mSSID);
                    }
                }
            }

            final NetworkInfo ni = mConnMgr.getActiveNetworkInfo();
            if (ni != null) {
                if (ni.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                    int type = ni.getType();
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        mNET.getWifiInfo();
                        if (mNET.mSSID != null) {
                            mNET.getIp();
                            mIPStr = getString(R.string.net_ip, mNET.mIP, mNET.mCIDR, mNET.mInterface);
                            mINStr = getString(R.string.net_ssid, mNET.mSSID);
                            mMOStr = getString(R.string.net_mode, getString(
                                    R.string.net_mode_wifi, mNET.mSpeed, WifiInfo.LINK_SPEED_UNITS));
                            setButtons(false);
                        }
                    } else if (type == ConnectivityManager.TYPE_MOBILE) { // 3G
                            mNET.getMobileInfo();
                            if (mNET.mCarrier != null) {
                                    mNET.getIp();
                                    mIPStr = getString(R.string.net_ip, mNET.mIP, mNET.mCIDR, mNET.mInterface);
                                    mINStr = getString(R.string.net_carrier, mNET.mCarrier);
                                    mMOStr = getString(R.string.net_mode,
                                        getString(R.string.net_mode_mobile));
                                    setButtons(false);
                                }
                    } else if (type == 3 || type == 9) { // ETH
                        mNET.getIp();
                        mIPStr = getString(R.string.net_ip, mNET.mIP, mNET.mCIDR, mNET.mInterface);
                        mINStr = "";
                        mMOStr = getString(R.string.net_mode) + getString(R.string.net_mode_eth);
                        setButtons(false);
                    } else {
                        mMOStr = getString(R.string.net_mode)
                                + getString(R.string.net_mode_unknown);
                    }
                } else {
                    cancelTasks();
                }
            } else {
                cancelTasks();
            }

            setInfo();
        }
    };
}
