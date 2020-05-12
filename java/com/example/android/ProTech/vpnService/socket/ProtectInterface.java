package com.example.android.ProTech.vpnService.socket;

import java.net.DatagramSocket;
import java.net.Socket;

public interface ProtectInterface {
	void protectSocket(Socket socket);
	void protectSocket(int socket);
	void protectSocket(DatagramSocket socket);
}
