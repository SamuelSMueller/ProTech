package com.example.android.ProTech.vpnService.transport.udp;

import android.support.annotation.NonNull;

import com.example.android.ProTech.vpnService.packet.PacketFactory;
import com.example.android.ProTech.vpnService.packet.PacketHeader;
import com.example.android.ProTech.vpnService.transport.tcp.HeaderExcept;
import com.example.android.ProTech.vpnService.util.Utilities;

import java.nio.ByteBuffer;

public class udpFactory {

	public static udpHeader createUDPHeader(@NonNull ByteBuffer stream) throws HeaderExcept {
		if ((stream.remaining()) < 8){
			throw new HeaderExcept("Minimum UDP header is 8 bytes.");
		}
		final int srcPort = stream.getShort();
		final int destPort = stream.getShort();
		final int length = stream.getShort();
		final int checksum = stream.getShort();

		return new udpHeader(srcPort, destPort, length, checksum);
	}


	public static byte[] createResponsePacket(PacketHeader ip, udpHeader udp, byte[] packetData){
		byte[] buffer;
		int udpLen = 8;
		if(packetData != null){
			udpLen += packetData.length;
		}
		int srcPort = udp.getmDestPrt();
		int destPort = udp.getmSrcPrt();
		short checksum = 0;
		
		PacketHeader ipHeader = PacketFactory.copyIPv4Header(ip);

		int srcIp = ip.getmDestIP();
		int destIp = ip.getmSrcIP();
		ipHeader.setmFragAllowed(false);
		ipHeader.setmSrcIP(srcIp);
		ipHeader.setmDestIP(destIp);
		ipHeader.setmID(Utilities.getmPacktID());

		int totalLength = ipHeader.getIPHeaderLength() + udpLen;
		
		ipHeader.setmLength(totalLength);
		buffer = new byte[totalLength];
		byte[] ipData = PacketFactory.createIPv4HeaderData(ipHeader);

		ipData[10] = ipData[11] = 0;

		byte[] ipChecksum = Utilities.calculateChecksum(ipData, 0, ipData.length);
		System.arraycopy(ipChecksum, 0, ipData, 10, 2);
		System.arraycopy(ipData, 0, buffer, 0, ipData.length);
		
		int start = ipData.length;
		byte[] intContainer = new byte[4];
		Utilities.writeIntToBytes(srcPort, intContainer, 0);
		System.arraycopy(intContainer,2,buffer,start,2);
		start += 2;
		
		Utilities.writeIntToBytes(destPort, intContainer, 0);
		System.arraycopy(intContainer, 2, buffer, start, 2);
		start += 2;
		
		Utilities.writeIntToBytes(udpLen, intContainer, 0);
		System.arraycopy(intContainer, 2, buffer, start, 2);
		start += 2;
		
		Utilities.writeIntToBytes(checksum, intContainer, 0);
		System.arraycopy(intContainer, 2, buffer, start, 2);
		start += 2;
		
		if (packetData != null)
		System.arraycopy(packetData, 0, buffer, start, packetData.length);

		return buffer;
	}
	
}
