package com.example.android.ProTech.vpnService.packet;

public class PacketHeader {
	private byte mIpVersion;

	private byte mHeaderLength;

	private byte mTypeOfService = 0;
	
	private byte mECN = 0;
	
	private int mLength = 0;
	
	private int mID = 0;
	
	private byte mFlag = 0;
	private boolean mFragAllowed;
	private boolean mFinalFrag;
	
	private short mFragOffset;
	
	private byte mTTL = 0;
	
	private byte mPrtcl = 0;
	
	private int mChecksum = 0;
	
	private int mSrcIP;
	
	private int mDestIP;

	public PacketHeader(byte mIpVersion, byte mHeaderLength,
						byte mTypeOfService, byte mECN, int mLength,
						int mID, boolean mFragAllowed,
						boolean mFinalFrag, short mFragOffset,
						byte mTTL, byte mPrtcl, int mChecksum,
						int mSrcIP, int mDestIP){
		this.mIpVersion = mIpVersion;
		this.mHeaderLength = mHeaderLength;
		this.mTypeOfService = mTypeOfService;
		this.mECN = mECN;
		this.mLength = mLength;
		this.mID = mID;
		this.mFragAllowed = mFragAllowed;
		if(mFragAllowed){
			this.mFlag |= 0x40;
		}
		this.mFinalFrag = mFinalFrag;
		if(mFinalFrag){
			this.mFlag |= 0x20;
		}
		this.mFragOffset = mFragOffset;
		this.mTTL = mTTL;
		this.mPrtcl = mPrtcl;
		this.mChecksum = mChecksum;
		this.mSrcIP = mSrcIP;
		this.mDestIP = mDestIP;
	}

	public byte getmIpVersion() {
		return mIpVersion;
	}

	byte getmHeaderLength() {
		return mHeaderLength;
	}

	public byte getmTypeOfService() {
		return mTypeOfService;
	}

	byte getmECN() {
		return mECN;
	}

	public int getmLength() {
		return mLength;
	}

	public int getIPHeaderLength(){
		return (mHeaderLength * 4);
	}

	public int getmID() {
		return mID;
	}

	public byte getmFlag() {
		return mFlag;
	}

	public boolean ismFragAllowed() {
		return mFragAllowed;
	}

	public boolean ismFinalFrag() {
		return mFinalFrag;
	}

	public short getmFragOffset() {
		return mFragOffset;
	}

	public byte getmTTL() {
		return mTTL;
	}

	public byte getmPrtcl() {
		return mPrtcl;
	}

	public int getmChecksum() {
		return mChecksum;
	}

	public int getmSrcIP() {
		return mSrcIP;
	}

	public int getmDestIP() {
		return mDestIP;
	}

	public void setmLength(int mLength) {
		this.mLength = mLength;
	}

	public void setmID(int mID) {
		this.mID = mID;
	}

	public void setmFragAllowed(boolean mFragAllowed) {
		this.mFragAllowed = mFragAllowed;
		if(mFragAllowed) {
			this.mFlag |= 0x40;
		} else {
			this.mFlag &= 0xBF;
		}
	}
	public void setmSrcIP(int mSrcIP) {
		this.mSrcIP = mSrcIP;
	}

	public void setmDestIP(int mDestIP) {
		this.mDestIP = mDestIP;
	}
}
