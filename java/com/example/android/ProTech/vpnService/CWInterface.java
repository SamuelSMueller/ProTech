package com.example.android.ProTech.vpnService;

import java.io.IOException;

public interface CWInterface {
	void write(byte[] data) throws IOException;
	void write(byte[] data, int offset, int count) throws IOException;
}
