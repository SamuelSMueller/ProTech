package com.example.android.ProTech.vpnService;

import android.support.annotation.NonNull;

import com.example.android.ProTech.vpnService.packet.PacketHeader;
import com.example.android.ProTech.vpnService.transport.headerInterface;
import com.example.android.ProTech.vpnService.transport.tcp.tcpHeader;
import com.example.android.ProTech.vpnService.transport.udp.udpHeader;


public class Packet {
	@NonNull
    private final PacketHeader mIpHeader;
	@NonNull
    private final headerInterface mTransHeader;
	@NonNull
    private final byte[] mBuffer;

	public Packet(@NonNull PacketHeader mIpHeader, @NonNull headerInterface mTransHeader, @NonNull byte[] data) {
		this.mIpHeader = mIpHeader;
		this.mTransHeader = mTransHeader;
		int transportLength;
		if (mTransHeader instanceof tcpHeader) {
			transportLength = ((tcpHeader) mTransHeader).getmOffset();
		} else if (mTransHeader instanceof udpHeader) {
			transportLength = 8;
		}
		mBuffer = data;
	}

	public byte getProtocol() {
		return mIpHeader.getmPrtcl();
	}

	@NonNull
	public headerInterface getmTransHeader() {
		return mTransHeader;
	}

	public int getSourcePort() {
		return mTransHeader.getmSrcPrt();
	}

	public int getDestinationPort() {
		return mTransHeader.getmDestPrt();
	}

	@NonNull
	public PacketHeader getmIpHeader() {
		return mIpHeader;
	}

	@NonNull
	public byte[] getmBuffer() {
		return mBuffer;
	}

}
