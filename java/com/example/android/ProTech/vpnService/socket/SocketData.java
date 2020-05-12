package com.example.android.ProTech.vpnService.socket;

import java.util.LinkedList;
import java.util.Queue;

public class SocketData {
	private static final SocketData mInst = new SocketData();
	private Queue<byte[]> mData;

	public static SocketData getmInst(){
		return mInst;
	}

	private SocketData() {
		mData = new LinkedList<>();
	}

	public synchronized void addData(byte[] packet) {
		mData.add(packet);
	}

	public synchronized byte[] getmData() {
			return mData.poll();
	}
}
