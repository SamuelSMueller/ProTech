package com.example.android.ProTech;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.widget.Button;

import com.example.android.ProTech.Network.HOST;

import java.lang.ref.WeakReference;

public abstract class discoAbstract extends AsyncTask<Void, HOST, Void> {


    protected int mHostFin = 0;
    final protected WeakReference<discoNetActivity> mDiscover;

    protected long mIP;
    protected long mStart = 0;
    protected long mEND = 0;
    protected long mSize = 0;
    private Button btn_trust;

    public discoAbstract(discoNetActivity discover) {
        mDiscover = new WeakReference<>(discover);
    }

    public void setNetwork(long ip, long start, long end) {
        this.mIP = ip;
        this.mStart = start;
        this.mEND = end;
    }

    public abstract void updateAllDB();
    public abstract void setDeviceDB(HOST host);


    abstract protected Void doInBackground(Void... params);

    @Override
    protected void onPreExecute() {
        mSize = (int) (mEND - mStart + 1);
        if (mDiscover != null) {
            final discoNetActivity discover = mDiscover.get();
            if (discover != null) {
                discover.setProgress(0);
            }
        }
    }

    @Override
    protected void onProgressUpdate(HOST... host) {
        if (mDiscover != null) {
            final discoNetActivity discover = mDiscover.get();
            if (discover != null) {
                if (!isCancelled()) {
                    if (host[0] != null) {
                        discover.addHost(host[0]);
                    }
                    if (mSize > 0) {
                        discover.setProgress((int) (mHostFin * 10000 / mSize));
                    }
                }

            }
        }
    }

    @Override
    protected void onPostExecute(Void unused) {
        if (mDiscover != null) {
            final discoNetActivity discover = mDiscover.get();

            if (discover != null) {
                Vibrator v = (Vibrator) discover.getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(discoNetActivity.mVibe);
            }
            discover.makeToast(R.string.discover_finished);
            btn_trust = (Button) discover.findViewById(R.id.button_safety);
            btn_trust.setEnabled(true);

            discover.stopDiscovering();
        }
    }

    @Override
    protected void onCancelled() {
        if (mDiscover != null) {
            final discoNetActivity discover = mDiscover.get();

            if (discover != null) {
                discover.makeToast(R.string.discover_canceled);
                btn_trust = (Button) discover.findViewById(R.id.button_safety);
                btn_trust.setEnabled(false);
                discover.stopDiscovering();
            }
        }
        super.onCancelled();
    }
}
