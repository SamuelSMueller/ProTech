package com.example.android.ProTech.vpnService.transport.tcp;

import android.support.annotation.Nullable;

import com.example.android.ProTech.vpnService.transport.headerInterface;


public class tcpHeader implements headerInterface {
	private int mSrcPrt;
	private int mDestPrt;
	private long mSeqNum;
	private int mOffset;
	private int mFlags;
	private boolean mIsNS = false;
	private boolean mIsCWR = false;
	private boolean mIsECE = false;
	private boolean mIsSyn = false;
	private boolean mIsAck = false;
	private boolean mIsFin = false;
	private boolean mIsRST = false;
	private boolean mIsPsh = false;
	private boolean mIsUrg = false;
	private int mWindowSize;
	private int mChecksum;
	private int mUrgPnt;
	@Nullable
    private byte[] mOpts;
	private long mAckNum;
	private int mMaxSeg = 0;
	private int mWindowScale = 0;
	private boolean mSACKAllowed = false;
	private int mStampSender = 0;
	private int mStampReply = 0;

	tcpHeader(int mSrcPrt, int mDestPrt, long mSeqNum, long mAckNum,
			  int mOffset, boolean isns, int mFlags,
			  int mWindowSize, int mChecksum, int mUrgPnt) {
		this.mSrcPrt = mSrcPrt;
		this.mDestPrt = mDestPrt;
		this.mSeqNum = mSeqNum;
		this.mOffset = mOffset;
		this.mIsNS = isns;
		this.mFlags = mFlags;
		this.mWindowSize = mWindowSize;
		this.mChecksum = mChecksum;
		this.mUrgPnt = mUrgPnt;
		this.mAckNum = mAckNum;
		setFlagBits();
	}
	private void setFlagBits() {
		mIsFin = (mFlags & 0x01) > 0;
		mIsSyn = (mFlags & 0x02) > 0;
		mIsRST = (mFlags & 0x04) > 0;
		mIsPsh = (mFlags & 0x08) > 0;
		mIsAck = (mFlags & 0x10) > 0;
		mIsUrg = (mFlags & 0x20) > 0;
		mIsECE = (mFlags & 0x40) > 0;
		mIsCWR = (mFlags & 0x80) > 0;
	}

	public boolean isNS(){
		return mIsNS;
	}
	void setIsNS(boolean isns){
		this.mIsNS = isns;
	}
	public boolean isCWR(){
		return mIsCWR;
	}
	void setIsCWR(boolean iscwr){
		this.mIsCWR = iscwr;
		if(iscwr){
			this.mFlags |= 0x80;
		}else{
			this.mFlags &= 0x7F;
		}
	}
	public boolean isECE(){
		return mIsECE;
	}
	void setIsECE(boolean isece){
		this.mIsECE = isece;
		if(isece){
			this.mFlags |= 0x40;
		}else{
			this.mFlags &= 0xBF;
		}
	}
	public boolean isSYN(){
		return mIsSyn;
	}
	void setIsSYN(boolean issyn){
		this.mIsSyn = issyn;
		if(issyn){
			this.mFlags |= 0x02;
		}else{
			this.mFlags &= 0xFD;
		}
	}
	public boolean isACK(){
		return mIsAck;
	}
	void setIsACK(boolean isack){
		this.mIsAck = isack;
		if(isack){
			this.mFlags |= 0x10;
		}else{
			this.mFlags &= 0xEF;
		}
	}
	public boolean isFIN(){
		return mIsFin;
	}
	void setIsFIN(boolean isfin){
		this.mIsFin = isfin;
		if(isfin){
			this.mFlags |= 0x1;
		}else{
			this.mFlags &= 0xFE;
		}
	}
	public boolean isRST(){
		return mIsRST;
	}
	void setIsRST(boolean isrst){
		this.mIsRST = isrst;
		if(isrst){
			this.mFlags |= 0x04;
		}else{
			this.mFlags &= 0xFB;
		}
	}
	public boolean isPSH(){
		return mIsPsh;
	}
	void setIsPSH(boolean ispsh){
		this.mIsPsh = ispsh;
		if(ispsh){
			this.mFlags |= 0x08;
		}else{
			this.mFlags &= 0xF7;
		}
	}
	public boolean isURG(){
		return mIsUrg;
	}
	void setIsURG(boolean isurg){
		this.mIsUrg = isurg;
		if(isurg){
			this.mFlags |= 0x20;
		}else{
			this.mFlags &= 0xDF;
		}
	}
	public int getmSrcPrt() {
		return mSrcPrt;
	}
	void setmSrcPrt(int mSrcPrt) {
		this.mSrcPrt = mSrcPrt;
	}
	public int getmDestPrt() {
		return mDestPrt;
	}
	void setmDestPrt(int mDestPrt) {
		this.mDestPrt = mDestPrt;
	}
	public long getmSeqNum() {
		return mSeqNum;
	}
	void setmSeqNum(long mSeqNum) {
		this.mSeqNum = mSeqNum;
	}
	public int getmOffset() {
		return mOffset;
	}
	int getmFlags() {
		return mFlags;
	}
	public int getmWindowSize() {
		return mWindowSize;
	}
	void setmWindowSize(int mWindowSize) {
		this.mWindowSize = mWindowSize;
	}
	public int getmChecksum() {
		return mChecksum;
	}
	public int getmUrgPnt() {
		return mUrgPnt;
	}
	@Nullable
    public byte[] getmOpts() {
		return mOpts;
	}
	void setmOpts(@Nullable byte[] mOpts) {
		this.mOpts = mOpts;
	}
	public long getmAckNum() {
		return mAckNum;
	}
	void setmAckNum(long mAckNum) {
		this.mAckNum = mAckNum;
	}
	public int getTCPHeaderLength(){
		return (mOffset * 4);
	}
	public int getmMaxSeg() {
		return mMaxSeg;
	}
	void setmMaxSeg(int mMaxSeg) {
		this.mMaxSeg = mMaxSeg;
	}
	public int getmWindowScale() {
		return mWindowScale;
	}
	void setmWindowScale(int mWindowScale) {
		this.mWindowScale = mWindowScale;
	}
	boolean ismSACKAllowed() {
		return mSACKAllowed;
	}
	void setmSACKAllowed(boolean isSelectiveAckPermitted) {
		this.mSACKAllowed = isSelectiveAckPermitted;
	}
	public int getmStampSender() {
		return mStampSender;
	}
	void setmStampSender(int mStampSender) {
		this.mStampSender = mStampSender;
	}
	int getmStampReply() {
		return mStampReply;
	}
	void setmStampReply(int mStampReply) {
		this.mStampReply = mStampReply;
	}
	
}
