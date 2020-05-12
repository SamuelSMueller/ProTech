package com.example.android.ProTech.vpnService.socket;

import java.util.ArrayList;
import java.util.List;

public class SDPush implements Runnable {
	private List<ReceiveInterface> mSubs;
	private SocketData mData;
	private volatile boolean mShutDown = false;

	public SDPush(){
		mData = SocketData.getmInst();
		mSubs = new ArrayList<>();
	}

	public void subscribe(ReceiveInterface subscriber){
		if(!mSubs.contains(subscriber)){
			mSubs.add(subscriber);
		}
	}

	@Override
	public void run() {
		
		while(!ismShutDown()) {
			byte[] packetData = mData.getmData();
			if(packetData != null) {
				for(ReceiveInterface subscriber: mSubs){
					subscriber.receive(packetData);
				}
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	private boolean ismShutDown() {
		return mShutDown;
	}
	public void setmShutDown(boolean mShutDown) {
		this.mShutDown = mShutDown;
	}

	
}
