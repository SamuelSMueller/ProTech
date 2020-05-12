package com.example.android.ProTech.vpnService.transport.tcp;

public class HeaderExcept extends Exception {
	private static final long mSerUID = 1L;
	public HeaderExcept(String message){
		super(message);
	}
}
