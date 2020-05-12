package com.example.android.ProTech.vpnService.packet;

import android.support.annotation.NonNull;

import com.example.android.ProTech.vpnService.transport.tcp.HeaderExcept;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class PacketFactory {

	@NonNull
	public static PacketHeader copyIPv4Header(@NonNull PacketHeader packetHeader) {
		return new PacketHeader(packetHeader.getmIpVersion(),
				packetHeader.getmHeaderLength(),
				packetHeader.getmTypeOfService(), packetHeader.getmECN(),
				packetHeader.getmLength(), packetHeader.getmID(),
				packetHeader.ismFragAllowed(), packetHeader.ismFinalFrag(),
				packetHeader.getmFragOffset(), packetHeader.getmTTL(),
				packetHeader.getmPrtcl(), packetHeader.getmChecksum(),
				packetHeader.getmSrcIP(), packetHeader.getmDestIP());
	}


	public static byte[] createIPv4HeaderData(@NonNull PacketHeader header){
		final byte[] buffer = new byte[header.getIPHeaderLength()];

		buffer[0] = (byte)((header.getmHeaderLength() & 0xF) | 0x40);
		buffer[1] = (byte) ((header.getmTypeOfService() << 2) & (header.getmECN() & 0xFF));
		buffer[2] = (byte)(header.getmLength() >> 8);
		buffer[3] = (byte)header.getmLength();
		buffer[4] = (byte)(header.getmID() >> 8);
		buffer[5] = (byte)header.getmID();

		//combine flags and partial fragment offset
		buffer[6] = (byte)(((header.getmFragOffset() >> 8) & 0x1F) | header.getmFlag());
		buffer[7] = (byte)header.getmFragOffset();
		buffer[8] = header.getmTTL();
		buffer[9]= header.getmPrtcl();
		buffer[10] = (byte) (header.getmChecksum() >> 8);
		buffer[11] = (byte)header.getmChecksum();

		final ByteBuffer buf = ByteBuffer.allocate(8);

		buf.order(ByteOrder.BIG_ENDIAN);
		buf.putInt(0,header.getmSrcIP());
		buf.putInt(4,header.getmDestIP());

		System.arraycopy(buf.array(), 0, buffer, 12, 4);
		System.arraycopy(buf.array(), 4, buffer, 16, 4);

		return buffer;
	}


	public static PacketHeader createIPv4Header(@NonNull ByteBuffer stream) throws HeaderExcept {
		if (stream.remaining() < 20) {
			throw new HeaderExcept("Minimum IPv4 header is 20 bytes.");
		}

		final byte versionAndHeaderLength = stream.get();
		final byte ipVersion = (byte) (versionAndHeaderLength >> 4);
		if (ipVersion != 0x04) {
			throw new HeaderExcept("IP version should be 4.");
		}

		final byte internetHeaderLength = (byte) (versionAndHeaderLength & 0x0F);
		if(stream.capacity() < internetHeaderLength * 4) {
			throw new HeaderExcept("Not enough space for IP header");
		}

		final byte dscpAndEcn = stream.get();
		final byte dscp = (byte) (dscpAndEcn >> 2);
		final byte ecn = (byte) (dscpAndEcn & 0x03);
		final int totalLength = stream.getShort();
		final int identification = stream.getShort();
		final short flagsAndFragmentOffset = stream.getShort();
		final boolean mayFragment = (flagsAndFragmentOffset & 0x4000) != 0;
		final boolean lastFragment = (flagsAndFragmentOffset & 0x2000) != 0;
		final short fragmentOffset = (short) (flagsAndFragmentOffset & 0x1FFF);
		final byte timeToLive = stream.get();
		final byte protocol = stream.get();
		final int checksum = stream.getShort();
		final int sourceIp = stream.getInt();
		final int desIp = stream.getInt();
		if (internetHeaderLength > 5) {
			for (int i = 0; i < internetHeaderLength - 5; i++) {
				stream.getInt();
			}
		}
		return new PacketHeader(ipVersion, internetHeaderLength, dscp, ecn, totalLength, identification,
				mayFragment, lastFragment, fragmentOffset, timeToLive, protocol, checksum, sourceIp, 
				desIp);
	}
}
