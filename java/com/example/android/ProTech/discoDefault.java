package com.example.android.ProTech;

import android.content.Context;
import android.os.SystemClock;

import com.example.android.ProTech.Database.Devices;
import com.example.android.ProTech.Database.Networks;
import com.example.android.ProTech.Network.MAC;
import com.example.android.ProTech.Network.HOST;
import com.example.android.ProTech.Network.NETDATA;
import com.example.android.ProTech.Network.RATE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class discoDefault extends discoAbstract {

    private final static int[] mPrts = { 139, 445, 22, 80 };
    private final static int mTimeScn = 3600;
    private final static int mTimeout = 10;
    private final static int mThreads = 10;
    private final int mRateMult = 5; //
    private int mPtrSlide = 2;
    private ExecutorService mPool;
    private boolean mRateControl;
    private RATE mRATE;
    private final String mQString = "https://api.macaddress.io/v1?apiKey=at_toIp36n72rokysNa5x8ySUMnitsiM&output=vendor&search=";
    //private final String queryString = "https://api.macvendors.com/"; //This API is less reliable than the one above but has no usage limits.

    private Context mCtx;

    private ArrayList<Devices> mDevs = new ArrayList<>();
    Networks mNetwork = new Networks();

    public discoDefault(discoNetActivity discover, Context ctx) {
        super(discover);
        mRATE = new RATE();
        //AndroidResolverConfigProvider.setContext(ctx);
        mCtx = ctx;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mDiscover != null) {
            final discoNetActivity discover = mDiscover.get();
            if (discover != null) {
                mRateControl = true;
            }
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (mDiscover != null) {
            final discoNetActivity discover = mDiscover.get();
            if (discover != null) {

                mPool = Executors.newFixedThreadPool(mThreads);
                if (mIP <= mEND && mIP >= mStart) {

                    launch(mStart);

                    long pt_backward = mIP;
                    long pt_forward = mIP + 1;
                    long size_hosts = mSize - 1;

                    for (int i = 0; i < size_hosts; i++) {
                        if (pt_backward <= mStart) {
                            mPtrSlide = 2;
                        } else if (pt_forward > mEND) {
                            mPtrSlide = 1;
                        }
                        if (mPtrSlide == 1) {
                            launch(pt_backward);
                            pt_backward--;
                            mPtrSlide = 2;
                        } else if (mPtrSlide == 2) {
                            launch(pt_forward);
                            pt_forward++;
                            mPtrSlide = 1;
                        }
                    }
                } else {
                    for (long i = mStart; i <= mEND; i++) {
                        launch(i);
                    }
                }



                mPool.shutdown();
                try {
                    if(!mPool.awaitTermination(mTimeScn, TimeUnit.SECONDS)){
                        mPool.shutdownNow();

                        if(!mPool.awaitTermination(mTimeout, TimeUnit.SECONDS)){

                        }
                    }
                } catch (InterruptedException e){

                    mPool.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
            checkAndUpdate();
        }
        return null;
    }

    @Override
    protected void onCancelled() {
        if (mPool != null) {
            synchronized (mPool) {
                mPool.shutdownNow();
            }
        }
        super.onCancelled();
    }


    private void launch(long i) {
        if(!mPool.isShutdown()) {
            mPool.execute(new CheckRunnable(NETDATA.getIpFromLongUnsigned(i)));
        }
    }

    private int getRate() {
        if (mRateControl) {
            return mRATE.mRate;
        }

        if (mDiscover != null) {
            final discoNetActivity discover = mDiscover.get();
            if (discover != null) {
                return 500;
            }
        }
        return 1;
    }

    private class CheckRunnable implements Runnable {
        private String addr;

        CheckRunnable(String addr) {
            this.addr = addr;
        }

        public void run() {
            if(isCancelled()) {
                publish(null);
            }

            final HOST host = new HOST();
            host.mResponseTime = getRate();
            host.mIPAddr = addr;
            try {
                InetAddress h = InetAddress.getByName(addr);
                if (mRateControl && mRATE.mInd != null && mHostFin % mRateMult == 0) {
                    mRATE.adaptRate();
                }
                host.mMACAddr = MAC.mGetMAC(addr);
                if (!NETDATA.mNoMac.equals(host.mMACAddr)) {

                    host.mVendor = getNicVendor(host);
                    setDeviceDB(host);

                    publish(host);
                    return;
                }
                if (h.isReachable(getRate())) {

                    host.mVendor = getNicVendor(host);
                    setDeviceDB(host);

                    publish(host);
                    if (mRateControl && mRATE.mInd == null) {
                        mRATE.mInd = addr;
                        mRATE.adaptRate();
                    }
                    return;
                }
                host.mMACAddr = MAC.mGetMAC(addr);
                if (!NETDATA.mNoMac.equals(host.mMACAddr)) {
                    host.mVendor = getNicVendor(host);
                    setDeviceDB(host);

                    publish(host);
                    return;


                }

            }catch (IOException e) {
                publish(null);

            } 
        }
    }



    private String sendQuery(String qMac) throws IOException {
        String result = "";

        URL searchURL = new URL(mQString + qMac);

        HttpURLConnection httpURLConnection = (HttpURLConnection) searchURL.openConnection();

        if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(
                    inputStreamReader,
                    8192);

            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }

            bufferedReader.close();
        }

        return result;
    }

    private String getNicVendor(HOST host) {
        String company = "Unknown1";
        try {
            String qMac = host.mMACAddr;
            SystemClock.sleep(20);
            company = sendQuery(qMac);
            host.mVendor = company;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Something is broken");
        }
        return company;
    }

    public void setDeviceDB(HOST host){
        Devices device = new Devices();
        final discoNetActivity discover = mDiscover.get();
        String[] SSIDS = new String[1];
        SSIDS[0] = discover.mNET.mSSID;
        List<Devices> devList;
        devList = dbClient.getInstance(mCtx).getAppDatabase()
                .networkDao()
                .getNetDevicesDB(SSIDS);

        if(devList.size() == 0){
            device.setMAC(host.mMACAddr);
            device.setNSSID(discover.mNET.mSSID);
            device.setAccessPoint((discover.mNET.mGateway.equals(host.mIPAddr)));
        }
        else{
            for(int i = 0; i<devList.size(); i++) {
                if (devList.get(i).getMAC().equals(host.mMACAddr)) {
                    device.setOldNSSID(devList.get(i).getNSSID());
                    device.setOldAccessPoint(devList.get(i).isAccessPoint());
                    device.setNSSID(discover.mNET.mSSID);
                    device.setAccessPoint((discover.mNET.mGateway.equals(host.mIPAddr)));
                }
                else{
                    device.setMAC(host.mMACAddr);
                    device.setNSSID(discover.mNET.mSSID);
                    device.setAccessPoint((discover.mNET.mGateway.equals(host.mIPAddr)));
                }
            }
        }
        devList.clear();
        mDevs.add(device);

    }


    private void checkAndUpdate() {


        final discoNetActivity discover = mDiscover.get();
        String[] SSIDS = new String[1];
        SSIDS[0] = discover.mNET.mSSID;
        List<Networks> networks;
        networks = dbClient.getInstance(mCtx).getAppDatabase()
                .networkDao()
                .getNetworkDB(SSIDS);


        if (networks.size() <= 0) {
            mNetwork.setSSID(discover.mNET.mSSID);

            mNetwork.setConCount(1);
            mNetwork.setBSSID(discover.mNET.mBSSID);
            mNetwork.setSecurity(discover.mNET.mAuth);
            mNetwork.setNumDev(mDevs.size());
            mNetwork.setConSpeed(discover.mNET.mSpeed);
            if (discover.mNET.mAuth.equals("OPEN")) {
                mNetwork.setPublic(true);
            } else {
                mNetwork.setPublic(false);
            }
            updateAllDB();
        } else{
            Networks net = networks.get(0);
            mNetwork.setSSID(networks.get(0).getSSID());

            mNetwork.setOldBSSID(net.getBSSID());
            mNetwork.setOldConCount(net.getConCount());
            mNetwork.setOldConSpeed(net.getConSpeed());
            mNetwork.setOldNumDev(net.getNumDev());
            mNetwork.setOldSecurity(net.getSecurity());
            mNetwork.setOldIsPublic(net.getPublic());

            mNetwork.setBSSID(discover.mNET.mBSSID);
            mNetwork.setConCount(net.getConCount() + 1);
            mNetwork.setConSpeed(discover.mNET.mSpeed);
            mNetwork.setNumDev(mDevs.size());
            mNetwork.setSecurity(discover.mNET.mAuth);
            if (discover.mNET.mAuth.equals("OPEN")) {
                mNetwork.setPublic(true);
            }
            else{
                mNetwork.setPublic(false);
            }
        }

        updateAllDB();
        mDevs.clear();
    }


    public void updateAllDB(){
        dbClient.getInstance(mCtx).getAppDatabase()
                .networkDao()
                .insertAll(mNetwork, mDevs);
    }


    private void publish(final HOST host) {
        mHostFin++;
        if(host == null){
            publishProgress((HOST) null);
            return; 
        }

        if (mDiscover != null) {
            final discoNetActivity discover = mDiscover.get();
            if (discover != null) {
                if(NETDATA.mNoMac.equals(host.mMACAddr)){
                    host.mMACAddr = MAC.mGetMAC(host.mIPAddr);
                }

                if (discover.mNET.mGateway.equals(host.mIPAddr)) {
                    host.mDeviceType = HOST.mGateWayType;
                }

                if (host.mHostName == null){
                    try {
                        host.mHostName = (InetAddress.getByName(host.mIPAddr)).getCanonicalHostName();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        publishProgress(host);
    }
}
