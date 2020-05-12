package com.example.android.ProTech.vpnService.socket;

import android.support.annotation.NonNull;

import com.example.android.ProTech.vpnService.CWInterface;
import com.example.android.ProTech.vpnService.sashManage;
import com.example.android.ProTech.vpnService.sesh;
import com.example.android.ProTech.vpnService.packet.PacketFactory;
import com.example.android.ProTech.vpnService.packet.PacketHeader;
import com.example.android.ProTech.vpnService.transport.tcp.HeaderExcept;
import com.example.android.ProTech.vpnService.transport.tcp.tcpHeader;
import com.example.android.ProTech.vpnService.transport.tcp.tcpFactory;
import com.example.android.ProTech.vpnService.transport.udp.udpFactory;
import com.example.android.ProTech.vpnService.transport.udp.udpHeader;
import com.example.android.ProTech.vpnService.util.Utilities;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Date;

class SDRead implements Runnable {
	private CWInterface mWriter;
	private String mSeshKey;
	private SocketData mSocketData;

	SDRead(CWInterface mWriter, String mSeshKey) {
		mSocketData = SocketData.getmInst();
		this.mWriter = mWriter;
		this.mSeshKey = mSeshKey;
	}

	@Override
	public void run() {
		sesh sesh = sashManage.INSTANCE.getSessionByKey(mSeshKey);
		if(sesh == null) {
			return;
		}

		AbstractSelectableChannel channel = sesh.getmChannel();

		if(channel instanceof SocketChannel) {
			readTCP(sesh);
		} else if(channel instanceof DatagramChannel){
			readUDP(sesh);
		} else {
			return;
		}

		if(sesh.ismAbortCon()) {
			sesh.getSelectionKey().cancel();
			if (channel instanceof SocketChannel){
				try {
					SocketChannel socketChannel = (SocketChannel) channel;
					if (socketChannel.isConnected()) {
						socketChannel.close();
					}
				} catch (IOException e) {
				}
			} else {
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
		} else {
			sesh.setBusyread(false);
		}
	}
	
	private void readTCP(@NonNull sesh sesh) {
		if(sesh.ismAbortCon()){
			return;
		}

		SocketChannel channel = (SocketChannel) sesh.getmChannel();
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER.mReceiveBuffer);
		int len;

		try {
			do {
				if(!sesh.isClientWindowFull()) {
					len = channel.read(buffer);
					if(len > 0) {
						sendToRequester(buffer, len, sesh);
						buffer.clear();
					} else if(len == -1) {
						sendFin(sesh);
						sesh.setmAbortCon(true);
					}
				} else {
					break;
				}
			} while(len > 0);
		}catch(NotYetConnectedException e){
		}catch(ClosedByInterruptException e){
		}catch(ClosedChannelException e){
		} catch (IOException e) {
			sesh.setmAbortCon(true);
		}
	}
	
	private void sendToRequester(ByteBuffer buffer, int dataSize, @NonNull sesh sesh){
		if(dataSize < BUFFER.mReceiveBuffer)
			sesh.setmRcvdLast(true);
		else
			sesh.setmRcvdLast(false);

		buffer.limit(dataSize);
		buffer.flip();
		byte[] data = new byte[dataSize];
		System.arraycopy(buffer.array(), 0, data, 0, dataSize);
		sesh.addReceivedData(data);
		while(sesh.hasReceivedData()){
			pushDataToClient(sesh);
		}
	}

	private void pushDataToClient(@NonNull sesh sesh){
		if(!sesh.hasReceivedData()){ }

		PacketHeader ipHeader = sesh.getmIpHeaderLast();
		tcpHeader tcpheader = sesh.getmTcpHeaderLast();
		int max = sesh.getmMaxSegSize() - 60;

		if(max < 1) {
			max = 1024;
		}

		byte[] packetBody = sesh.getReceivedData(max);
		if(packetBody != null && packetBody.length > 0) {
			long unAck = sesh.getmSndNxt();
			long nextUnAck = sesh.getmSndNxt() + packetBody.length;
			sesh.setmSndNxt(nextUnAck);
			sesh.setmUnACKData(packetBody);
			sesh.setmResnd(0);

			byte[] data = tcpFactory.createResponsePacketData(ipHeader,
					tcpheader, packetBody, sesh.hasReceivedLastSegment(),
					sesh.getmRecSeq(), unAck,
					sesh.getmStampSndr(), sesh.getmStampReply());
			try {
				mWriter.write(data);
				mSocketData.addData(data);
			} catch (IOException e) {
			}
		}
	}
	private void sendFin(sesh sesh){
		final PacketHeader ipHeader = sesh.getmIpHeaderLast();
		final tcpHeader tcpheader = sesh.getmTcpHeaderLast();
		final byte[] data = tcpFactory.createFinData(ipHeader, tcpheader,
				sesh.getmSndNxt(), sesh.getmRecSeq(),
				sesh.getmStampSndr(), sesh.getmStampReply());
		try {
			mWriter.write(data);
			mSocketData.addData(data);
		} catch (IOException e) {
		}
	}
	private void readUDP(sesh sesh){
		DatagramChannel channel = (DatagramChannel) sesh.getmChannel();
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER.mReceiveBuffer);
		int len;

		try {
			do{
				if(sesh.ismAbortCon()){
					break;
				}
				len = channel.read(buffer);
				if(len > 0){
					Date date = new Date();
					long responseTime = date.getTime() - sesh.mConStart;
					
					buffer.limit(len);
					buffer.flip();

					byte[] data = new byte[len];
					System.arraycopy(buffer.array(),0, data, 0, len);
					byte[] packetData = udpFactory.createResponsePacket(
							sesh.getmIpHeaderLast(), sesh.getmUdpHeaderLast(), data);

					mWriter.write(packetData);

					mSocketData.addData(packetData);
					buffer.clear();
					
					try {
						final ByteBuffer stream = ByteBuffer.wrap(packetData);
						PacketHeader ip = PacketFactory.createIPv4Header(stream);
						udpHeader udp = udpFactory.createUDPHeader(stream);
						String str = Utilities.getUDPoutput(ip, udp);
					} catch (HeaderExcept e) {
						e.printStackTrace();
					}
				}
			} while(len > 0);
		}catch(NotYetConnectedException ex){
		} catch (IOException e) {
			e.printStackTrace();
			sesh.setmAbortCon(true);
		}
	}
}
