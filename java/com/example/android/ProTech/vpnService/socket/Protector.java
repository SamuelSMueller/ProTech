package com.example.android.ProTech.vpnService.socket;

import java.net.DatagramSocket;
import java.net.Socket;

public class Protector {
	private static final Object mSynObj = new Object();
	private static volatile Protector mInst = null;
	private ProtectInterface mProt = null;

	public static Protector getmInst(){
		if(mInst == null){
			synchronized(mSynObj){
				if(mInst == null){
					mInst = new Protector();
				}
			}
		}
		return mInst;
	}

	public void setmProt(ProtectInterface mProt){
		if(this.mProt == null){
			this.mProt = mProt;
		}
	}
	public void protect(Socket socket){
		mProt.protectSocket(socket);
	}
	public void protect(int socket){
		mProt.protectSocket(socket);
	}
	public void protect(DatagramSocket socket){
		mProt.protectSocket(socket);
	}
}
