package com.example.android.ProTech.vpnService;


import com.example.android.ProTech.vpnService.packet.PacketHeader;
import com.example.android.ProTech.vpnService.transport.tcp.tcpHeader;
import com.example.android.ProTech.vpnService.transport.udp.udpHeader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.spi.AbstractSelectableChannel;

public class sesh {

	private AbstractSelectableChannel mChannel;
	
	private int mDestIP = 0;
	private int mDestPrt = 0;
	
	private int mSrcIP = 0;
	private int mSrcPrt = 0;

	private long mRecSeq = 0;

	private long mUnACK = 0;
	private boolean mIsAcked = false;

	private long mSndNxt = 0;
	private int mSndWndw = 0;
	private int mSndWndwSz = 0;
	private int mSndWndwScl = 0;

	private volatile int mAmtLastAcked = 0;

	private int mMaxSegSize = 0;

	private boolean mIsCon = false;

	private ByteArrayOutputStream mInStream;
	
	private ByteArrayOutputStream mOutStream;
	
	private boolean mRcvdLast = false;
	
	private PacketHeader mIpHeaderLast;
	private tcpHeader mTcpHeaderLast;
	private udpHeader mUdpHeaderLast;

	private boolean mEndCon = false;
	
	private boolean mOutReady = false;
	
	private byte[] mUnACKData = null;
	
	private boolean mCorrupt = false;
	
	private int mResnd = 0;
	
	private int mStampSndr = 0;

	private int mStampReply = 0;
	
	private boolean mToFin = false;

	private volatile boolean mReadBusy = false;

	private volatile boolean mWriteBusy = false;
	
	private volatile boolean mAbortCon = false;
	
	private SelectionKey mSelKey = null;
	
	public long mConStart = 0;
	
	sesh(int mSrcIP, int mSrcPrt, int destinationIp, int destinationPort){
		mInStream = new ByteArrayOutputStream();
		mOutStream = new ByteArrayOutputStream();
		this.mSrcIP = mSrcIP;
		this.mSrcPrt = mSrcPrt;
		this.mDestIP = destinationIp;
		this.mDestPrt = destinationPort;
	}



	public boolean isClientWindowFull(){
		return (mSndWndw > 0 && mAmtLastAcked >= mSndWndw) ||
				(mSndWndw == 0 && mAmtLastAcked > 65535);
	}

	public synchronized void addReceivedData(byte[] data){
		try {
			mInStream.write(data);
		} catch (IOException e) {
		}
	}


	public synchronized byte[] getReceivedData(int maxSize){
		byte[] data = mInStream.toByteArray();
		mInStream.reset();
		if(data.length > maxSize){
			byte[] small = new byte[maxSize];
			System.arraycopy(data, 0, small, 0, maxSize);
			int len = data.length - maxSize;
			mInStream.write(data, maxSize, len);
			data = small;
		}
		return data;
	}

	public boolean hasReceivedData(){
		return mInStream.size() > 0;
	}

	synchronized int setSendingData(ByteBuffer data) {
		final int remaining = data.remaining();
		mOutStream.write(data.array(), data.position(), data.remaining());
		return remaining;
	}

	int getSendingDataSize(){
		return mOutStream.size();
	}

	public synchronized byte[] getSendingData(){
		byte[] data = mOutStream.toByteArray();
		mOutStream.reset();
		return data;
	}

	public boolean hasDataToSend(){
		return mOutStream.size() > 0;
	}

	public int getmDestIP() {
		return mDestIP;
	}

	public int getmDestPrt() {
		return mDestPrt;
	}

	long getmUnACK() {
		return mUnACK;
	}

	void setmUnACK(long mUnACK) {
		this.mUnACK = mUnACK;
	}

	public long getmSndNxt() {
		return mSndNxt;
	}

	public void setmSndNxt(long mSndNxt) {
		this.mSndNxt = mSndNxt;
	}

	int getmSndWndw() {
		return mSndWndw;
	}

	public int getmMaxSegSize() {
		return mMaxSegSize;
	}

	void setmMaxSegSize(int mMaxSegSize) {
		this.mMaxSegSize = mMaxSegSize;
	}

	public boolean ismIsCon() {
		return mIsCon;
	}

	public void setmIsCon(boolean isConnected) {
		this.mIsCon = isConnected;
	}

	public int getmSrcIP() {
		return mSrcIP;
	}

	public int getmSrcPrt() {
		return mSrcPrt;
	}

	void setSendWindowSizeAndScale(int sendWindowSize, int sendWindowScale) {
		this.mSndWndwSz = sendWindowSize;
		this.mSndWndwScl = sendWindowScale;
		this.mSndWndw = sendWindowSize * sendWindowScale;
	}

	int getmSndWndwScl() {
		return mSndWndwScl;
	}

	void setAcked(boolean isacked) {
		this.mIsAcked = isacked;
	}

	public long getmRecSeq() {
		return mRecSeq;
	}

	void setmRecSeq(long mRecSeq) {
		this.mRecSeq = mRecSeq;
	}

	public AbstractSelectableChannel getmChannel() {
		return mChannel;
	}

	public void setmChannel(AbstractSelectableChannel mChannel) {
		this.mChannel = mChannel;
	}

	public boolean hasReceivedLastSegment() {
		return mRcvdLast;
	}
	public void setmRcvdLast(boolean mRcvdLast) {
		this.mRcvdLast = mRcvdLast;
	}
	public synchronized PacketHeader getmIpHeaderLast() {
		return mIpHeaderLast;
	}
	synchronized void setmIpHeaderLast(PacketHeader mIpHeaderLast) {
		this.mIpHeaderLast = mIpHeaderLast;
	}
	public synchronized tcpHeader getmTcpHeaderLast() {
		return mTcpHeaderLast;
	}
	synchronized void setmTcpHeaderLast(tcpHeader mTcpHeaderLast) {
		this.mTcpHeaderLast = mTcpHeaderLast;
	}
	
	public synchronized udpHeader getmUdpHeaderLast() {
		return mUdpHeaderLast;
	}
	synchronized void setmUdpHeaderLast(udpHeader mUdpHeaderLast) {
		this.mUdpHeaderLast = mUdpHeaderLast;
	}
	boolean ismEndCon() {
		return mEndCon;
	}
	void setmEndCon(boolean mEndCon) {
		this.mEndCon = mEndCon;
	}
	public boolean ismOutReady() {
		return mOutReady;
	}
	void setmOutReady(boolean isDataForSendingReady) {
		this.mOutReady = isDataForSendingReady;
	}

	public void setmUnACKData(byte[] mUnACKData) {
		this.mUnACKData = mUnACKData;
	}
	

	void setmCorrupt(boolean mCorrupt) {
		this.mCorrupt = mCorrupt;
	}

	public void setmResnd(int mResnd) {
		this.mResnd = mResnd;
	}
	public int getmStampSndr() {
		return mStampSndr;
	}
	void setmStampSndr(int mStampSndr) {
		this.mStampSndr = mStampSndr;
	}
	public int getmStampReply() {
		return mStampReply;
	}
	void setmStampReply(int mStampReply) {
		this.mStampReply = mStampReply;
	}
	boolean ismToFin() {
		return mToFin;
	}

	public boolean ismReadBusy() {
		return mReadBusy;
	}
	public void setBusyread(boolean isbusyread) {
		this.mReadBusy = isbusyread;
	}
	public boolean isBusywrite() {
		return mWriteBusy;
	}
	public void setBusywrite(boolean isbusywrite) {
		this.mWriteBusy = isbusywrite;
	}
	public boolean ismAbortCon() {
		return mAbortCon;
	}
	public void setmAbortCon(boolean mAbortCon) {
		this.mAbortCon = mAbortCon;
	}
	public SelectionKey getSelectionKey() {
		return mSelKey;
	}
	void setSelectionKey(SelectionKey selectionkey) {
		this.mSelKey = selectionkey;
	}
}
