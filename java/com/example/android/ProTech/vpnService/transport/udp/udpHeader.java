package com.example.android.ProTech.vpnService.transport.udp;
import com.example.android.ProTech.vpnService.transport.headerInterface;

public class udpHeader implements headerInterface {
	private int mSrcPrt;
	private int mDestPrt;
	private int mLen;
	private int mChecksum;

	udpHeader(int srcPort, int destPort, int mLen, int mChecksum){
		this.mSrcPrt = srcPort;
		this.mDestPrt = destPort;
		this.mLen = mLen;
		this.mChecksum = mChecksum;
	}
	public int getmSrcPrt() {
		return mSrcPrt;
	}
	public int getmDestPrt() {
		return mDestPrt;
	}
	public int getmLen() {
		return mLen;
	}
	public void setmLen(int mLen) {
		this.mLen = mLen;
	}
	public int getmChecksum() {
		return mChecksum;
	}
	
}
