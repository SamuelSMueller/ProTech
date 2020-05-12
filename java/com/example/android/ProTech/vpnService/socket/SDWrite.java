package com.example.android.ProTech.vpnService.socket;
import android.support.annotation.NonNull;

import com.example.android.ProTech.vpnService.CWInterface;
import com.example.android.ProTech.vpnService.sashManage;
import com.example.android.ProTech.vpnService.sesh;
import com.example.android.ProTech.vpnService.transport.tcp.tcpFactory;
import com.example.android.ProTech.vpnService.util.Utilities;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Date;

public class SDWrite implements Runnable {

	private static CWInterface mWriter;
	@NonNull
    private String mSeshKey;

	SDWrite(CWInterface mWriter, @NonNull String mSeshKey) {
		this.mWriter = mWriter;
		this.mSeshKey = mSeshKey;
	}

	@Override
	public void run() {
		final sesh sesh = sashManage.INSTANCE.getSessionByKey(mSeshKey);
		if(sesh == null) {
			return;
		}

		sesh.setBusywrite(true);

		AbstractSelectableChannel channel = sesh.getmChannel();
		if(channel instanceof SocketChannel){
			writeTCP(sesh);
		}else if(channel instanceof DatagramChannel){
			writeUDP(sesh);
		} else {
			return;
		}
		sesh.setBusywrite(false);

		if(sesh.ismAbortCon()){
			sesh.getSelectionKey().cancel();

			if(channel instanceof SocketChannel) {
				try {
					SocketChannel socketChannel = (SocketChannel) channel;
					if (socketChannel.isConnected()) {
						socketChannel.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if(channel instanceof DatagramChannel) {
				try {
					DatagramChannel datagramChannel = (DatagramChannel) channel;
					if (datagramChannel.isConnected()) {
						datagramChannel.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			sashManage.INSTANCE.closeSession(sesh);
		}
	}

	private void writeUDP(sesh sesh){
		if(!sesh.hasDataToSend()){
			return;
		}
		DatagramChannel channel = (DatagramChannel) sesh.getmChannel();
		String name = Utilities.intToIPAddress(sesh.getmDestIP())+":"+ sesh.getmDestPrt()+
				"-"+ Utilities.intToIPAddress(sesh.getmSrcIP())+":"+ sesh.getmSrcPrt();
		byte[] data = sesh.getSendingData();
		ByteBuffer buffer = ByteBuffer.allocate(data.length);
		buffer.put(data);
		buffer.flip();
		try {
			String str = new String(data);
			channel.write(buffer);
			Date dt = new Date();
			sesh.mConStart = dt.getTime();
		}catch(NotYetConnectedException ex2){
			sesh.setmAbortCon(true);
		} catch (IOException e) {
			sesh.setmAbortCon(true);
			e.printStackTrace();
		}
	}
	
	private void writeTCP(sesh sesh){
		SocketChannel channel = (SocketChannel) sesh.getmChannel();

		String name = Utilities.intToIPAddress(sesh.getmDestIP())+":"+ sesh.getmDestPrt()+
				"-"+ Utilities.intToIPAddress(sesh.getmSrcIP())+":"+ sesh.getmSrcPrt();
		
		byte[] data = sesh.getSendingData();
		ByteBuffer buffer = ByteBuffer.allocate(data.length);
		buffer.put(data);
		buffer.flip();
		
		try {
			channel.write(buffer);
		} catch (NotYetConnectedException ex) {
		} catch (IOException e) {

			byte[] rstData = tcpFactory.createRstData(
					sesh.getmIpHeaderLast(), sesh.getmTcpHeaderLast(), 0);
			try {
				mWriter.write(rstData);
				SocketData socketData = SocketData.getmInst();
				socketData.addData(rstData);
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			sesh.setmAbortCon(true);
		}
	}
}
