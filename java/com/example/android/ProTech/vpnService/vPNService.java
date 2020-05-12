package com.example.android.ProTech.vpnService;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.view.inputmethod.InputMethodManager;

import com.example.android.ProTech.vpnService.socket.ProtectInterface;
import com.example.android.ProTech.vpnService.socket.ReceiveInterface;
import com.example.android.ProTech.vpnService.socket.SDPush;
import com.example.android.ProTech.vpnService.socket.SDService;
import com.example.android.ProTech.vpnService.socket.Protector;
import com.example.android.ProTech.vpnService.transport.tcp.HeaderExcept;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class vPNService extends VpnService implements Handler.Callback,
        Runnable, ProtectInterface, ReceiveInterface {
	private static final int MAX_PACKET_LEN = 1500;

	private Handler mHandler;
	private Thread mThread;
	private ParcelFileDescriptor mInterface;
	private boolean mValidServ;
	private SDService mDataServ;
	private Thread mDServThread;
	private SDPush mBGWriter;
	private Thread mQThread;
	private boolean mIsVuln;
	private boolean mIsRecent = false;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {



		if (mHandler == null) {
			mHandler = new Handler(this);
		}

		if (mThread != null) {
			mThread.interrupt();
			int reps = 0;
			while(mThread.isAlive()){

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		mThread = new Thread(this, "CaptureThread");
		mThread.start();
		return START_STICKY;
	}


	private void unregisterAnalyzerCloseCmdReceiver() {
		try {
			if (serviceCloseCmdReceiver != null) {
				unregisterReceiver(serviceCloseCmdReceiver);
				serviceCloseCmdReceiver = null;
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void onRevoke() {
		super.onRevoke();
	}

	private BroadcastReceiver serviceCloseCmdReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			unregisterAnalyzerCloseCmdReceiver();
			mValidServ = false;
			stopSelf();
		}
	};

	@Override
	public ComponentName startService(Intent service) {
		return super.startService(service);
	}


	@Override
	public boolean stopService(Intent name) {

		mValidServ = false;
		return super.stopService(name);
	}

	@Override
	public void protectSocket(Socket socket) {
		this.protect(socket);
	}

	@Override
	public void protectSocket(int socket) {
		this.protect(socket);
	}


	@Override
	public void receive(byte[] packet) {

	}


	@Override
	public void onDestroy() {

		mValidServ = false;

		unregisterAnalyzerCloseCmdReceiver();

		if (mDataServ !=  null)
			mDataServ.setmShutDown(true);

		if (mBGWriter != null)
			mBGWriter.setmShutDown(true);


		if(mDServThread != null){
			mDServThread.interrupt();
		}
		if(mQThread != null){
			mQThread.interrupt();
		}

		try {
			if (mInterface != null) {
				mInterface.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (mThread != null) {
			mThread.interrupt();
			int reps = 0;
			while(mThread.isAlive()){

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(reps > 5){
					break;
				}
			}
			mThread = null;
		}

	}

	@Override
	public void run() {

		Protector protector = Protector.getmInst();
		protector.setmProt(this);

		try {
			if (startVpnService()) {
				startCapture();

			} else {

			}
		} catch (IOException e) {

		}
	}

	boolean startVpnService() throws IOException {
		if (mInterface != null) {

			return false;
		}



		Builder builder = new Builder()
				.addAddress("10.120.0.1", 32)
				.addRoute("0.0.0.0", 0)
				.setSession("Protection Service");
		mInterface = builder.establish();

		if(mInterface != null){

			return true;
		} else {

			return false;
		}
	}

	void startCapture() throws IOException {


		FileInputStream clientReader = new FileInputStream(mInterface.getFileDescriptor());

		FileOutputStream clientWriter = new FileOutputStream(mInterface.getFileDescriptor());

		ByteBuffer packet = ByteBuffer.allocate(MAX_PACKET_LEN);
		CWInterface clientPacketWriter = new ClientWriter(clientWriter);

		seshHandle handler = seshHandle.getInstance();
		handler.setmWriter(clientPacketWriter);

		mDataServ = new SDService(clientPacketWriter);
		mDServThread = new Thread(mDataServ);
		mDServThread.start();

		mBGWriter = new SDPush();
		mBGWriter.subscribe(this);
		mQThread = new Thread(mBGWriter);
		mQThread.start();

		byte[] data;
		int length;
		mValidServ = true;
		while (mValidServ) {
			data = packet.array();
			length = clientReader.read(data);
			if (length > 0) {
				try {
					packet.limit(length);

					handler.handlePacket(packet);
				} catch (HeaderExcept e) {
				}
				mIsVuln = handler.getVuln();

				if(mIsVuln && !mIsRecent) {
					InputMethodManager imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
					imeManager.showInputMethodPicker();
					mIsRecent = true;
					resRecent();
				}

				handler.resetVuln();
				packet.clear();
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {

				}
			}
		}
	}


	private void resRecent(){
		final Handler handler = mHandler;
		Runnable runnable;
		final int TIMER_DELAY = 60000;


		runnable = new Runnable(){
			@Override
			public void run() {
				handler.postDelayed(this, TIMER_DELAY); // repeat again
				mIsRecent = false;
			}
		};
			handler.postDelayed(runnable, TIMER_DELAY);
		}


	@Override
	public boolean handleMessage(Message message) {
		return true; //this function is necessary, but useless
	}

	@Override
	public void protectSocket(DatagramSocket socket) {
		this.protect(socket);
	}

}
