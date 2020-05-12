package com.example.android.ProTech;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.example.android.ProTech.Network.HOST;
import com.example.android.ProTech.Network.NETDATA;

import java.util.ArrayList;
import java.util.List;

final public class discoNetActivity extends netActivity implements OnItemClickListener {

    public final static long mVibe = (long) 250;
    public final static int mPortRes = 1;
    private static LayoutInflater mInflater;
    private int mCurrentNet = 0;
    private long mNetIP = 0;
    private long mNetStart = 0;
    private long mNetEnd = 0;
    private List<HOST> mHOSTS = null;
    private HostsAdapter mAdapt;
    private Button btn_discover;
    private Button btn_trust;
    private discoAbstract mDiscoveryTask = null;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Please Grant Location Permission")
                        .setMessage("In this version of Android, Location Permission is required in order to scan nearby networks.")
                        .setPositiveButton("Ok", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(discoNetActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            btn_discover.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startDiscovering();
                }
            });
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        btn_discover.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                startDiscovering();
                            }
                        });

                    }

                } else {

                    makeToast(R.string.PermDenied);
                    super.finish();

                }
                return;
            }

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.discovery);
        mInflater = LayoutInflater.from(mCtx);
        btn_discover = (Button) findViewById(R.id.btn_discover);
        btn_trust = (Button) findViewById(R.id.button_safety);
        btn_trust.setEnabled(false);
        checkLocationPermission();


        mAdapt = new HostsAdapter(mCtx);
        ListView list = (ListView) findViewById(R.id.output);
        list.setAdapter(mAdapt);
        list.setItemsCanFocus(false);
        list.setOnItemClickListener(this);
        list.setEmptyView(findViewById(R.id.list_empty));

    }

    @Override
    public void onResume() {
        super.onResume();
    }



    protected void setInfo() {
        ((TextView) findViewById(R.id.info_ip)).setText(mIPStr);
        ((TextView) findViewById(R.id.info_in)).setText(mINStr);
        ((TextView) findViewById(R.id.info_mo)).setText(mMOStr);

        if (mDiscoveryTask != null) {
            setButton(btn_discover, R.drawable.cancel, false);
            btn_discover.setText(R.string.btn_discover_cancel);
            btn_discover.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    cancelTasks();
                }
            });
        }

        if (mCurrentNet != mNET.hashCode()) {
            mCurrentNet = mNET.hashCode();

            // Cancel running tasks
            cancelTasks();
        } else {
            return;
        }

        mNetIP = NETDATA.getUnsignedLongFromIp(mNET.mIP);

        int shift = (32 - mNET.mCIDR);
        if (mNET.mCIDR < 31) {
            mNetStart = (mNetIP >> shift << shift) + 1;
            mNetEnd = (mNetStart | ((1 << shift) - 1)) - 1;
        } else {
            mNetStart = (mNetIP >> shift << shift);
            mNetEnd = (mNetStart | ((1 << shift) - 1));
        }

    }

    protected void setButtons(boolean disable) {
        if (disable) {
            setButtonOff(btn_discover, R.drawable.disabled);
        } else {
            setButtonOn(btn_discover, R.drawable.discover);
        }
    }

    protected void cancelTasks() {
        if (mDiscoveryTask != null) {
            mDiscoveryTask.cancel(true);
            mDiscoveryTask = null;
        }
    }


    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        final HOST host = mHOSTS.get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(discoNetActivity.this);
        dialog.setTitle(R.string.discover_action_title);
        dialog.setItems(new CharSequence[] {
                getString(R.string.discover_action_rename) }, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:

                        final View v = mInflater.inflate(R.layout.dialog_edittext, null);
                        final EditText txt = (EditText) v.findViewById(R.id.edittext);


                        final AlertDialog.Builder rename = new AlertDialog.Builder(
                                discoNetActivity.this);
                        rename.setView(v);
                        rename.setTitle(R.string.discover_action_rename);
                        rename.setPositiveButton(R.string.btn_ok, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                final String name = txt.getText().toString();
                                host.mHostName = name;
                                mAdapt.notifyDataSetChanged();
                                Toast.makeText(discoNetActivity.this,
                                        R.string.discover_action_saved, Toast.LENGTH_SHORT).show();
                            }
                        });
                        rename.setNegativeButton(R.string.btn_remove, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                host.mHostName = null;
                                mAdapt.notifyDataSetChanged();
                                Toast.makeText(discoNetActivity.this,
                                        R.string.discover_action_deleted, Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
                        rename.show();
                        break;
                }
            }
        });
        dialog.setNegativeButton(R.string.btn_discover_cancel, null);
        dialog.show();
    }

    static class ViewHolder {
        TextView host;
        TextView mac;
        TextView vendor;
        ImageView logo;
    }


    private class HostsAdapter extends ArrayAdapter<Void> {
        public HostsAdapter(Context ctxt) {
            super(ctxt, R.layout.list_host, R.id.list);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_host, null);
                holder = new ViewHolder();
                holder.host = (TextView) convertView.findViewById(R.id.list);
                holder.mac = (TextView) convertView.findViewById(R.id.mac);
                holder.vendor = (TextView) convertView.findViewById(R.id.vendor);
                holder.logo = (ImageView) convertView.findViewById(R.id.logo);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final HOST host = mHOSTS.get(position);
            if (host.mDeviceType == HOST.mGateWayType) {
                holder.logo.setImageResource(R.drawable.router);
            } else if (host.mAlive == 1 || !host.mMACAddr.equals(NETDATA.mNoMac)) {
                holder.logo.setImageResource(R.drawable.computer);
            } else {
                holder.logo.setImageResource(R.drawable.computer_down);
            }
            if (host.mHostName != null && !host.mHostName.equals(host.mIPAddr)) {
                holder.host.setText(host.mHostName + " (" + host.mIPAddr + ")");
            } else {
                holder.host.setText(host.mIPAddr);
            }
            if (!host.mMACAddr.equals(NETDATA.mNoMac)) {
                holder.mac.setText(host.mMACAddr);
                if(host.mVendor != null){
                    holder.vendor.setText(host.mVendor);
                }
                else {
                    holder.vendor.setText(R.string.info_unknown);
                }
                holder.mac.setVisibility(View.VISIBLE);
                holder.vendor.setVisibility(View.VISIBLE);
            } else {
                holder.mac.setVisibility(View.VISIBLE);
                holder.vendor.setVisibility(View.VISIBLE);
            }
            return convertView;
        }
    }


    private void startDiscovering() {
        mDiscoveryTask = new discoDefault(discoNetActivity.this, getApplicationContext());

        mDiscoveryTask.setNetwork(mNetIP, mNetStart, mNetEnd);
        mDiscoveryTask.execute();
        btn_discover.setText(R.string.btn_discover_cancel);
        setButton(btn_discover, R.drawable.cancel, false);
        btn_discover.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cancelTasks();
            }
        });
        makeToast(R.string.discover_start);
        setProgressBarVisibility(true);
        setProgressBarIndeterminateVisibility(true);
        initList();
    }

    public void stopDiscovering() {
        mDiscoveryTask = null;
        setButtonOn(btn_discover, R.drawable.discover);
        btn_discover.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startDiscovering();
            }
        });
        setProgressBarVisibility(false);
        setProgressBarIndeterminateVisibility(false);

        btn_discover.setText(R.string.btn_discover);
    }

    private void initList() {
        mAdapt.clear();
        mHOSTS = new ArrayList<HOST>();
    }

    public void addHost(HOST host) {
        host.mPos = mHOSTS.size();
        mHOSTS.add(host);
        mAdapt.add(null);
    }

    public void makeToast(int msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void setButton(Button btn, int res, boolean disable) {
        if (disable) {
            setButtonOff(btn, res);
        } else {
            setButtonOn(btn, res);
        }
    }

    private void setButtonOff(Button b, int drawable) {
        b.setClickable(false);
        b.setEnabled(false);
        b.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0);
    }

    private void setButtonOn(Button b, int drawable) {
        b.setClickable(true);
        b.setEnabled(true);
        b.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0);
    }

    public void sendMessage3(View view) {
        Intent intent = new Intent(this, trustActivity.class);
        Bundle bundle= new Bundle();

        startActivity(intent);
    }
}
