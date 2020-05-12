package com.example.android.ProTech;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.ProTech.Database.Devices;
import com.example.android.ProTech.Database.Networks;
import com.example.android.ProTech.Network.NETDATA;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class trustActivity extends AppCompatActivity {

    private List<Networks> mNets;
    private List<Devices> mDevs;
    private List<Devices> mNewDevs;
    private Networks mNet;
    protected NETDATA NETDATA = null;
    private ConnectivityManager mConMgr;
    private double TrustScore;
    private ArrayList<String> mDetailsGood;
    private ArrayList<String> mDetailsBad;
    private ListView mBadList;
    private ListView mGoodList;
    private ProgressBar mPgsBar;
    private ArrayAdapter<String> mBadAdapt;
    private ArrayAdapter<String> mGoodAdapt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trust);
        mConMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NETDATA = new NETDATA(this);
        getNetInfo();
        mDetailsBad = new ArrayList<String>();
        mDetailsGood = new ArrayList<String>();

        mNewDevs = new ArrayList<Devices>();

        mBadAdapt = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,android.R.id.text1, mDetailsBad);
        mGoodAdapt = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,android.R.id.text1, mDetailsGood);

        mBadList = (ListView)findViewById(R.id.BadList);
        mGoodList = (ListView)findViewById(R.id.GoodList);

        mBadList.setAdapter(mBadAdapt);
        mGoodList.setAdapter(mGoodAdapt);

        mPgsBar = (ProgressBar) findViewById(R.id.pBar);
    }

    private void getNetInfo() {
        final NetworkInfo ni = mConMgr.getActiveNetworkInfo();
        if (ni != null) {
            if (ni.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                int type = ni.getType();
                if (type == ConnectivityManager.TYPE_WIFI) { // WIFI
                    NETDATA.getWifiInfo();
                }
            }
        }
    }

    public void getCurrentDB() {
        if (NETDATA.mSSID != null) {
            String[] SSIDS = new String[1];
            SSIDS[0] = NETDATA.mSSID;

            mNets = dbClient.getInstance(this).getAppDatabase()
                    .networkDao()
                    .getNetworkDB(SSIDS);

            if (mNets.size() > 0) {
                mNet = mNets.get(0);
            }

            mDevs = dbClient.getInstance(this).getAppDatabase()
                    .networkDao()
                    .getNetDevicesDB(SSIDS);

            if (mDevs.size() > 0) {
                for (int i = 0; i < mDevs.size(); i++) {
                }
            }


            calculateTrust();
        }else{

        }
    }


    public void calculateTrust(){
        mDetailsBad.clear();
        mDetailsGood.clear();
        mNewDevs.clear();
        TrustScore = 0.0;
            if(mNet.getConCount() == 1){
                mDetailsBad.add("New Network");
                boolean pub = mNet.getPublic();
                if(pub){
                        mDetailsBad.add("No Authentication Security");
                }else{
                    String auth = NETDATA.mAuth;
                    TrustScore += 1.0;
                    switch(auth){
                        case "WEP":
                                TrustScore += 0.25;
                                mDetailsGood.add("Has Authentication Security");
                                mDetailsBad.add("WEP Authentication is Vulnerable");
                                break;
                        case "WPA":
                                TrustScore += 0.5;
                                mDetailsGood.add("Has Authentication Security");
                                mDetailsBad.add("WPA1 Authentication is Vulnerable");
                                break;
                        case "WPA2":
                                TrustScore += 1.0;
                                mDetailsGood.add("Has Authentication Security");
                                mDetailsGood.add("WPA2 Authentication is Strong");
                                break;
                        case "WPA3":
                                TrustScore += 1.0;
                                mDetailsGood.add("Has Authentication Security");
                                mDetailsGood.add("WPA3 Authentication is Strong");
                                break;
                    }
                }
            }


            else {
                if(mNet.getBSSID().equals(mNet.getOldBSSID())) {
                    TrustScore += 1.0;
                    mDetailsGood.add("Recognized Access Point");
                }
                else{
                    mDetailsBad.add("Unrecognized Access Point");
                }

                if(  (double) mNet.getConSpeed() <= (double)(mNet.getOldConSpeed() * 1.5) && (double) mNet.getConSpeed() >= (double)(mNet.getOldConSpeed()*0.5) ){ //check that the new value is within 50% of its previous value
                    TrustScore += 0.75;
                    if(  (double) mNet.getConSpeed() <= (double)(mNet.getOldConSpeed() * 1.25) && (double) mNet.getConSpeed() >= (double)(mNet.getOldConSpeed()*0.75) ) { //check that the new value is within %25 of its previous value
                        TrustScore += 0.25;
                    }
                    mDetailsGood.add("Consistent Network Connection");
                }
                else{
                    mDetailsBad.add("Inconsistent Network Connection");
                }

                boolean pub = mNet.getPublic();
                if(pub){
                    mDetailsBad.add("No Authentication Security");
                }else {
                    TrustScore += 1.0;
                    String auth = NETDATA.mAuth;
                    switch (auth) {
                        case "WEP":
                            TrustScore += 0.25;
                            mDetailsGood.add("Has Authentication Security");
                            mDetailsBad.add("WEP Authentication is Vulnerable");
                            break;
                        case "WPA":
                            TrustScore += 0.5;
                            mDetailsGood.add("Has Authentication Security");
                            mDetailsBad.add("WPA1 Authentication is Vulnerable");
                            break;
                        case "WPA2":
                            TrustScore += 1.0;
                            mDetailsGood.add("Has Authentication Security");
                            mDetailsGood.add("WPA2 Authentication is Strong");
                            break;
                        case "WPA3":
                            TrustScore += 1.0;
                            mDetailsGood.add("Has Authentication Security");
                            mDetailsGood.add("WPA3 Authentication is Strong but Experimental");
                            break;
                    }

                    if (mDevs.size() > 0) {
                        for (int i = 0; i < mDevs.size(); i++) {
                            if(mDevs.get(i).getOldNSSID() == null){
                                mNewDevs.add(mDevs.get(i)); //TODO: ADD TOUCH OPTION TO VIEW UNRECOGNIZED DEVICES
                                mDetailsBad.add("Unrecognized Device: "+ mDevs.get(i).getMAC());
                            }
                        }

                        if(mNewDevs.size() == 0){
                            mDetailsGood.add("All devices recognized");
                        }

                        double ratio = (  (double)(mNewDevs.size()) / (double)(mDevs.size())  );
                        TrustScore += (1-ratio);
                    }
                }
            }
        }


    public void getData(View view){
        BackgroundWork bW = new BackgroundWork(this);
        bW.execute();
    }

    public class BackgroundWork extends AsyncTask<Void, Void, Void> {

        private WeakReference<Context> mCtx;

        BackgroundWork(Context ctx) {
            mCtx = new WeakReference<>(ctx);
        }

        protected Void doInBackground(Void... params){
           getCurrentDB();
           return null;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... params) {
        }

        @Override
        protected void onPostExecute(Void unused) {
                    Context ctx = mCtx.get();
                    Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(discoNetActivity.mVibe);
                    TextView scoreText = (TextView) findViewById(R.id.textView_6);
                    scoreText.setText( String.format(Locale.US,"%.2f" ,TrustScore) );
                    mPgsBar.setProgress((int)(TrustScore * 20));
                    mBadAdapt.notifyDataSetChanged();
                    mGoodAdapt.notifyDataSetChanged();
        }

    }




























}