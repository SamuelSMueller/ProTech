package com.example.android.ProTech.vpnService;

import java.io.FileOutputStream;
import java.io.IOException;

class ClientWriter implements CWInterface {
	private FileOutputStream mWriter;

	ClientWriter(FileOutputStream mWriter){
		this.mWriter = mWriter;
	}

	@Override
	public synchronized void write(byte[] data) throws IOException {
		mWriter.write(data);
	}

	@Override
	public synchronized void write(byte[] data, int offset, int count) throws IOException {
		mWriter.write(data, offset, count);
	}
}
