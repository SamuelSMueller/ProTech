package com.example.android.ProTech.vpnService;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.ProTech.vpnService.socket.BUFFER;
import com.example.android.ProTech.vpnService.socket.SDService;
import com.example.android.ProTech.vpnService.socket.Protector;
import com.example.android.ProTech.vpnService.util.Utilities;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum sashManage {
	INSTANCE;

	private final Map<String, sesh> mTable = new ConcurrentHashMap<>();
	private Protector mProt = Protector.getmInst();
	private Selector mSel;

	sashManage() {
		try {
			mSel = Selector.open();
		} catch (IOException e) {
		}
	}

	public Selector getmSel(){
		return mSel;
	}

	public void keepSessionAlive(sesh sesh) {
		if(sesh != null){
			String key = createKey(sesh.getmDestIP(), sesh.getmDestPrt(),
					sesh.getmSrcIP(), sesh.getmSrcPrt());
			mTable.put(key, sesh);
		}
	}

	public int addClientData(ByteBuffer buffer, sesh sesh) {
		if (buffer.limit() <= buffer.position())
			return 0;
		return sesh.setSendingData(buffer);
	}

	public sesh getSession(int ip, int port, int srcIp, int srcPort) {
		String key = createKey(ip, port, srcIp, srcPort);

		return getSessionByKey(key);
	}

	@Nullable
	public sesh getSessionByKey(String key) {
		if(mTable.containsKey(key)){
			return mTable.get(key);
		}

		return null;
	}

	public sesh getSessionByChannel(AbstractSelectableChannel channel) {
		Collection<sesh> seshes = mTable.values();

		for (sesh sesh : seshes) {
			if (channel == sesh.getmChannel()) {
				return sesh;
			}
		}

		return null;
	}

	public void closeSession(int ip, int port, int srcIp, int srcPort){
		String key = createKey(ip, port, srcIp, srcPort);
		sesh sesh = mTable.remove(key);

		if(sesh != null) {
			final AbstractSelectableChannel channel = sesh.getmChannel();
			try {
				if (channel != null) {
					channel.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void closeSession(@NonNull sesh sesh){
		String key = createKey(sesh.getmDestIP(),
				sesh.getmDestPrt(), sesh.getmSrcIP(),
				sesh.getmSrcPrt());
		mTable.remove(key);

		try {
			AbstractSelectableChannel channel = sesh.getmChannel();
			if(channel != null) {
				channel.close();
			}
		} catch (IOException e) {
		}
	}

	@Nullable
	public sesh createNewUDPSession(int ip, int port, int srcIp, int srcPort){
		String keys = createKey(ip, port, srcIp, srcPort);

		if (mTable.containsKey(keys))
			return mTable.get(keys);

		sesh sesh = new sesh(srcIp, srcPort, ip, port);

		DatagramChannel channel;

		try {
			channel = DatagramChannel.open();
			channel.socket().setSoTimeout(0);
			channel.configureBlocking(false);

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		mProt.protect(channel.socket());

		String ips = Utilities.intToIPAddress(ip);
		String sourceIpAddress = Utilities.intToIPAddress(srcIp);
		SocketAddress socketAddress = new InetSocketAddress(ips, port);

		try {
			channel.connect(socketAddress);
			sesh.setmIsCon(channel.isConnected());
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}

		try {
			synchronized(SDService.mSyncSel2) {
				mSel.wakeup();
				synchronized(SDService.mSyncSel) {
					SelectionKey selectionKey;
					if (channel.isConnected()) {
						selectionKey = channel.register(mSel, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
					} else {
						selectionKey = channel.register(mSel, SelectionKey.OP_CONNECT | SelectionKey.OP_READ |
								SelectionKey.OP_WRITE);
					}
					sesh.setSelectionKey(selectionKey);
				}
			}
		} catch (ClosedChannelException e) {
			e.printStackTrace();

			return null;
		}

		sesh.setmChannel(channel);

		if (mTable.containsKey(keys)) {
			try {
				channel.close();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			mTable.put(keys, sesh);
		}

		return sesh;
	}

	@Nullable
	public sesh createNewSession(int ip, int port, int srcIp, int srcPort){
		String key = createKey(ip, port, srcIp, srcPort);
		if (mTable.containsKey(key)) {

			return null;
		}

		sesh sesh = new sesh(srcIp, srcPort, ip, port);

		SocketChannel channel;
		try {
			channel = SocketChannel.open();
			channel.socket().setKeepAlive(true);
			channel.socket().setTcpNoDelay(true);
			channel.socket().setSoTimeout(0);
			channel.socket().setReceiveBufferSize(BUFFER.mReceiveBuffer);
			channel.configureBlocking(false);
		}catch(SocketException e){

			return null;
		} catch (IOException e) {
			return null;
		}
		String ips = Utilities.intToIPAddress(ip);

		mProt.protect(channel.socket());


		SocketAddress socketAddress = new InetSocketAddress(ips, port);

		boolean connected;
		try{
			connected = channel.connect(socketAddress);
		} catch(IOException e) {
			return null;
		}

		sesh.setmIsCon(connected);

		try {
			synchronized(SDService.mSyncSel2){
				mSel.wakeup();
				synchronized(SDService.mSyncSel){
					SelectionKey selectionKey = channel.register(mSel,
							SelectionKey.OP_CONNECT | SelectionKey.OP_READ |
									SelectionKey.OP_WRITE);
					sesh.setSelectionKey(selectionKey);
				}
			}
		} catch (ClosedChannelException e) {
			e.printStackTrace();
			return null;
		}

		sesh.setmChannel(channel);

		if (mTable.containsKey(key)) {
			try {
				channel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		} else {
			mTable.put(key, sesh);
		}
		return sesh;
	}

	public String createKey(int ip, int port, int srcIp, int srcPort){
		return Utilities.intToIPAddress(srcIp) + ":" + srcPort + "-" +
				Utilities.intToIPAddress(ip) + ":" + port;
	}
}
