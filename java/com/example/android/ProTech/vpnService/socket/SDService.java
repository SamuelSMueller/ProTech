package com.example.android.ProTech.vpnService.socket;

import com.example.android.ProTech.vpnService.CWInterface;
import com.example.android.ProTech.vpnService.sashManage;
import com.example.android.ProTech.vpnService.sesh;
import com.example.android.ProTech.vpnService.util.Utilities;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class SDService implements Runnable {
	public static final Object mSyncSel = new Object();
	public static final Object mSyncSel2 = new Object();

	private static CWInterface mWriter;
	private volatile boolean mShutDown = false;
	private Selector mSel;
	private ThreadPoolExecutor mPool;
	
	public SDService(CWInterface CWInterface) {
		final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
		mPool = new ThreadPoolExecutor(8, 100, 10, TimeUnit.SECONDS, taskQueue);
		mWriter = CWInterface;
	}

	@Override
	public void run() {
		mSel = sashManage.INSTANCE.getmSel();
		runTask();
	}

	public void setmShutDown(boolean mShutDown){
		this.mShutDown = mShutDown;
		sashManage.INSTANCE.getmSel().wakeup();
	}

	private void runTask(){
		
		while(!mShutDown){
			try {
				synchronized(mSyncSel){
					mSel.select();
				}
			} catch (IOException e) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {

				}
				continue;
			}

			if(mShutDown){
				break;
			}
			synchronized(mSyncSel2){
				Iterator<SelectionKey> iterator = mSel.selectedKeys().iterator();
				while(iterator.hasNext()){
					SelectionKey key = iterator.next();
					SelectableChannel selectableChannel = key.channel();
					if(selectableChannel instanceof SocketChannel) {
						try {
							processTCPSelectionKey(key);
						} catch (IOException e) {
							key.cancel();
						}
					} else if (selectableChannel instanceof DatagramChannel) {
						processUDPSelectionKey(key);
					}
					iterator.remove();
					if(mShutDown){
						break;
					}
				}
			}
		}
	}

	private void processUDPSelectionKey(SelectionKey key){
		if(!key.isValid()){
			return;
		}
		DatagramChannel channel = (DatagramChannel) key.channel();
		sesh sesh = sashManage.INSTANCE.getSessionByChannel(channel);
		if(sesh == null){
			return;
		}
		
		if(!sesh.ismIsCon() && key.isConnectable()){
			String ips = Utilities.intToIPAddress(sesh.getmDestIP());
			int port = sesh.getmDestPrt();
			SocketAddress address = new InetSocketAddress(ips,port);
			try {
				channel = channel.connect(address);
				sesh.setmChannel(channel);
				sesh.setmIsCon(channel.isConnected());
			}catch (Exception e) {
				e.printStackTrace();
				sesh.setmAbortCon(true);
			}
		}
		if(channel.isConnected()){
			processSelector(key, sesh);
		}
	}

	private void processTCPSelectionKey(SelectionKey key) throws IOException {
		if(!key.isValid()){
			return;
		}
		SocketChannel channel = (SocketChannel)key.channel();
		sesh sesh = sashManage.INSTANCE.getSessionByChannel(channel);
		if(sesh == null){
			return;
		}
		
		if(!sesh.ismIsCon() && key.isConnectable()){
			String ips = Utilities.intToIPAddress(sesh.getmDestIP());
			int port = sesh.getmDestPrt();
			SocketAddress address = new InetSocketAddress(ips, port);
			boolean connected = false;
			if(!channel.isConnected() && !channel.isConnectionPending()){
				try{
					connected = channel.connect(address);
				} catch (ClosedChannelException | UnresolvedAddressException |
                        UnsupportedAddressTypeException | SecurityException e) {
					sesh.setmAbortCon(true);
				} catch (IOException e) {
					sesh.setmAbortCon(true);
				}
			}
			
			if (connected) {
				sesh.setmIsCon(connected);
			} else {
				if(channel.isConnectionPending()){
					connected = channel.finishConnect();
					sesh.setmIsCon(connected);
				}
			}
		}
		if(channel.isConnected()){
			processSelector(key, sesh);
		}
	}

	private void processSelector(SelectionKey selectionKey, sesh sesh){
		String sessionKey = sashManage.INSTANCE.createKey(sesh.getmDestIP(),
				sesh.getmDestPrt(), sesh.getmSrcIP(),
				sesh.getmSrcPrt());
		if(selectionKey.isValid() && selectionKey.isWritable()
				&& !sesh.isBusywrite() && sesh.hasDataToSend()
				&& sesh.ismOutReady())
		{
			sesh.setBusywrite(true);
			final SDWrite worker =
					new SDWrite(mWriter, sessionKey);
			mPool.execute(worker);
		}
		if(selectionKey.isValid() && selectionKey.isReadable()
				&& !sesh.ismReadBusy())
		{
			sesh.setBusyread(true);
			final SDRead worker =
					new SDRead(mWriter, sessionKey);
			mPool.execute(worker);
		}
	}
}
