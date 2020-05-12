package com.example.android.ProTech.vpnService.transport.tcp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.ProTech.vpnService.Packet;
import com.example.android.ProTech.vpnService.packet.PacketFactory;
import com.example.android.ProTech.vpnService.packet.PacketHeader;
import com.example.android.ProTech.vpnService.util.Utilities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.Random;

public class tcpFactory {
	
	private static tcpHeader copyTCPHeader(tcpHeader tcpheader){
		final tcpHeader tcp = new tcpHeader(tcpheader.getmSrcPrt(),
				tcpheader.getmDestPrt(), tcpheader.getmSeqNum(),
				tcpheader.getmAckNum(), tcpheader.getmOffset(), tcpheader.isNS(),
				tcpheader.getmFlags(), tcpheader.getmWindowSize(),
				tcpheader.getmChecksum(), tcpheader.getmUrgPnt());

		tcp.setmMaxSeg(65535);
		tcp.setmWindowScale(tcpheader.getmWindowScale());
		tcp.setmSACKAllowed(tcpheader.ismSACKAllowed());
		tcp.setmStampSender(tcpheader.getmStampSender());
		tcp.setmStampReply(tcpheader.getmStampReply());
		return tcp;
	}

	public static byte[] createFinAckData(PacketHeader packetHeader, tcpHeader tcpHeader,
										  long ackToClient, long seqToClient,
										  boolean isFin, boolean isAck){
		PacketHeader ip = PacketFactory.copyIPv4Header(packetHeader);
		com.example.android.ProTech.vpnService.transport.tcp.tcpHeader tcp = copyTCPHeader(tcpHeader);

		int sourceIp = ip.getmDestIP();
		int destIp = ip.getmSrcIP();
		int sourcePort = tcp.getmDestPrt();
		int destPort = tcp.getmSrcPrt();
		
		ip.setmDestIP(destIp);
		ip.setmSrcIP(sourceIp);
		tcp.setmDestPrt(destPort);
		tcp.setmSrcPrt(sourcePort);
		
		tcp.setmAckNum(ackToClient);
		tcp.setmSeqNum(seqToClient);
		
		ip.setmID(Utilities.getmPacktID());

		tcp.setIsACK(isAck);
		tcp.setIsSYN(false);
		tcp.setIsPSH(false);
		tcp.setIsFIN(isFin);

		tcp.setmStampReply(tcp.getmStampSender());
		Date currentDate = new Date();
		int senderTimestamp = (int)currentDate.getTime();
		tcp.setmStampSender(senderTimestamp);

		int totalLength = ip.getIPHeaderLength() + tcp.getTCPHeaderLength();
		
		ip.setmLength(totalLength);
		
		return createPacketData(ip, tcp, null);
	}

	public static byte[] createFinData(PacketHeader ip, tcpHeader tcp, long ackNumber, long seqNumber, int timeSender, int timeReplyTo){
		int sourceIp = ip.getmDestIP();
		int destIp = ip.getmSrcIP();
		int sourcePort = tcp.getmDestPrt();
		int destPort = tcp.getmSrcPrt();
		
		tcp.setmAckNum(ackNumber);
		tcp.setmSeqNum(seqNumber);
		
		tcp.setmStampReply(timeReplyTo);
		tcp.setmStampSender(timeSender);
		
		ip.setmDestIP(destIp);
		ip.setmSrcIP(sourceIp);
		tcp.setmDestPrt(destPort);
		tcp.setmSrcPrt(sourcePort);
		
		ip.setmID(Utilities.getmPacktID());
		
		tcp.setIsRST(false);
		tcp.setIsACK(true);
		tcp.setIsSYN(false);
		tcp.setIsPSH(false);
		tcp.setIsCWR(false);
		tcp.setIsECE(false);
		tcp.setIsFIN(true);
		tcp.setIsNS(false);
		tcp.setIsURG(false);

		tcp.setmOpts(null);

		tcp.setmWindowSize(0);

		int totalLength = ip.getIPHeaderLength() + tcp.getTCPHeaderLength();
		
		ip.setmLength(totalLength);
		
		return createPacketData(ip, tcp, null);
	}

	public static byte[] createRstData(PacketHeader ipheader, tcpHeader tcpheader, int datalength){
		PacketHeader ip = PacketFactory.copyIPv4Header(ipheader);
		tcpHeader tcp = copyTCPHeader(tcpheader);

		int sourceIp = ip.getmDestIP();
		int destIp = ip.getmSrcIP();
		int sourcePort = tcp.getmDestPrt();
		int destPort = tcp.getmSrcPrt();
		
		long ackNumber = 0;
		long seqNumber = 0;
		
		if(tcp.getmAckNum() > 0){
			seqNumber = tcp.getmAckNum();
		}else{
			ackNumber = tcp.getmSeqNum() + datalength;
		}
		tcp.setmAckNum(ackNumber);
		tcp.setmSeqNum(seqNumber);
		
		ip.setmDestIP(destIp);
		ip.setmSrcIP(sourceIp);
		tcp.setmDestPrt(destPort);
		tcp.setmSrcPrt(sourcePort);
		
		ip.setmID(0);
		
		tcp.setIsRST(true);
		tcp.setIsACK(false);
		tcp.setIsSYN(false);
		tcp.setIsPSH(false);
		tcp.setIsCWR(false);
		tcp.setIsECE(false);
		tcp.setIsFIN(false);
		tcp.setIsNS(false);
		tcp.setIsURG(false);

		tcp.setmOpts(null);

		tcp.setmWindowSize(0);

		int totalLength = ip.getIPHeaderLength() + tcp.getTCPHeaderLength();
		
		ip.setmLength(totalLength);
		
		return createPacketData(ip, tcp, null);
	}

	public static byte[] createResponseAckData(PacketHeader ipHeader, tcpHeader tcpheader, long ackToClient){
		PacketHeader ip = PacketFactory.copyIPv4Header(ipHeader);
		tcpHeader tcp = copyTCPHeader(tcpheader);
		
		int sourceIp = ip.getmDestIP();
		int destIp = ip.getmSrcIP();
		int sourcePort = tcp.getmDestPrt();
		int destPort = tcp.getmSrcPrt();
		
		long seqNumber = tcp.getmAckNum();
		
		ip.setmDestIP(destIp);
		ip.setmSrcIP(sourceIp);
		tcp.setmDestPrt(destPort);
		tcp.setmSrcPrt(sourcePort);
		
		tcp.setmAckNum(ackToClient);
		tcp.setmSeqNum(seqNumber);
		
		ip.setmID(Utilities.getmPacktID());

		tcp.setIsACK(true);
		tcp.setIsSYN(false);
		tcp.setIsPSH(false);
		tcp.setIsFIN(false);

		tcp.setmStampReply(tcp.getmStampSender());
		Date currentdate = new Date();
		int sendertimestamp = (int)currentdate.getTime();
		tcp.setmStampSender(sendertimestamp);

		int totalLength = ip.getIPHeaderLength() + tcp.getTCPHeaderLength();
		
		ip.setmLength(totalLength);
		
		return createPacketData(ip, tcp, null);
	}

	public static byte[] createResponsePacketData(PacketHeader ip, tcpHeader tcp, byte[] packetData, boolean isPsh,
												  long ackNumber, long seqNumber, int timeSender, int timeReplyto){
		PacketHeader ipHeader = PacketFactory.copyIPv4Header(ip);
		tcpHeader tcpHeader = copyTCPHeader(tcp);

		int sourceIp = ipHeader.getmDestIP();
		int sourcePort = tcpHeader.getmDestPrt();
		ipHeader.setmDestIP(ipHeader.getmSrcIP());
		ipHeader.setmSrcIP(sourceIp);
		tcpHeader.setmDestPrt(tcpHeader.getmSrcPrt());
		tcpHeader.setmSrcPrt(sourcePort);
		
		tcpHeader.setmAckNum(ackNumber);
		tcpHeader.setmSeqNum(seqNumber);
		
		ipHeader.setmID(Utilities.getmPacktID());

		tcpHeader.setIsACK(true);
		tcpHeader.setIsSYN(false);
		tcpHeader.setIsPSH(isPsh);
		tcpHeader.setIsFIN(false);
		
		tcpHeader.setmStampSender(timeSender);
		tcpHeader.setmStampReply(timeReplyto);

		int totalLength = ipHeader.getIPHeaderLength() + tcpHeader.getTCPHeaderLength();
		if(packetData != null){
			totalLength += packetData.length;
		}
		ipHeader.setmLength(totalLength);
		
		return createPacketData(ipHeader, tcpHeader, packetData);
	}

	public static Packet createSynAckPacketData(PacketHeader ip, tcpHeader tcp){
		PacketHeader ipheader = PacketFactory.copyIPv4Header(ip);
		tcpHeader tcpheader = copyTCPHeader(tcp);

		int sourceIp = ipheader.getmDestIP();
		int destIp = ipheader.getmSrcIP();
		int sourcePort = tcpheader.getmDestPrt();
		int destPort = tcpheader.getmSrcPrt();
		long ackNumber = tcpheader.getmSeqNum() + 1;
		long seqNumber;
		Random random = new Random();
		seqNumber = random.nextInt();
		if(seqNumber < 0){
			seqNumber = seqNumber * -1;
		}
		ipheader.setmDestIP(destIp);
		ipheader.setmSrcIP(sourceIp);
		tcpheader.setmDestPrt(destPort);
		tcpheader.setmSrcPrt(sourcePort);

		tcpheader.setmAckNum(ackNumber);

		tcpheader.setmSeqNum(seqNumber);

		tcpheader.setIsACK(true);
		tcpheader.setIsSYN(true);

		tcpheader.setmStampReply(tcpheader.getmStampSender());
		Date currentdate = new Date();
		int sendertimestamp = (int)currentdate.getTime();
		tcpheader.setmStampSender(sendertimestamp);
		
		return new Packet(ipheader, tcpheader, createPacketData(ipheader, tcpheader, null));
	}


    private static byte[] createPacketData(PacketHeader ipHeader, tcpHeader tcpheader, @Nullable byte[] data){
		int dataLength = 0;
		if(data != null){
			dataLength = data.length;
		}
		byte[] buffer = new byte[ipHeader.getIPHeaderLength() + tcpheader.getTCPHeaderLength() + dataLength];
		byte[] ipBuffer = PacketFactory.createIPv4HeaderData(ipHeader);
		byte[] tcpBuffer = createTCPHeaderData(tcpheader);
		
		System.arraycopy(ipBuffer, 0, buffer, 0, ipBuffer.length);
		System.arraycopy(tcpBuffer, 0, buffer, ipBuffer.length, tcpBuffer.length);
		if(dataLength > 0){
			int offset = ipBuffer.length + tcpBuffer.length;
			System.arraycopy(data, 0, buffer, offset, dataLength);
		}
		byte[] zero = {0, 0};
		System.arraycopy(zero, 0, buffer, 10, 2);
		byte[] ipChecksum = Utilities.calculateChecksum(buffer, 0, ipBuffer.length);
		System.arraycopy(ipChecksum, 0, buffer, 10, 2);

		int tcpStart = ipBuffer.length;
		System.arraycopy(zero, 0, buffer, tcpStart + 16, 2);
		byte[] tcpChecksum = Utilities.calculateTCPHeaderChecksum(buffer, tcpStart, tcpBuffer.length + dataLength ,
				ipHeader.getmDestIP(), ipHeader.getmSrcIP());

		System.arraycopy(tcpChecksum, 0, buffer,tcpStart + 16, 2);


		return buffer;
	}

	private static byte[] createTCPHeaderData(tcpHeader header){
		final byte[] buffer = new byte[header.getTCPHeaderLength()];
		buffer[0] = (byte)(header.getmSrcPrt() >> 8);
		buffer[1] = (byte)(header.getmSrcPrt());
		buffer[2] = (byte)(header.getmDestPrt() >> 8);
		buffer[3] = (byte)(header.getmDestPrt());

		final ByteBuffer sequenceNumber = ByteBuffer.allocate(4);
		sequenceNumber.order(ByteOrder.BIG_ENDIAN);
		sequenceNumber.putInt((int)header.getmSeqNum());

		System.arraycopy(sequenceNumber.array(), 0, buffer, 4, 4);

		final ByteBuffer ackNumber = ByteBuffer.allocate(4);
		ackNumber.order(ByteOrder.BIG_ENDIAN);
		ackNumber.putInt((int)header.getmAckNum());
		System.arraycopy(ackNumber.array(), 0, buffer, 8, 4);
		
		buffer[12] = (byte) (header.isNS() ? (header.getmOffset() << 4) | 0x1
				: header.getmOffset() << 4);
		buffer[13] = (byte)header.getmFlags();

		buffer[14] = (byte)(header.getmWindowSize() >> 8);
		buffer[15] = (byte)header.getmWindowSize();

		buffer[16] = (byte)(header.getmChecksum() >> 8);
		buffer[17] = (byte)header.getmChecksum();

		buffer[18] = (byte)(header.getmUrgPnt() >> 8);
		buffer[19] = (byte)header.getmUrgPnt();

		final byte[] options = header.getmOpts();
		if (options != null) {
			for (int i = 0; i < options.length; i++) {
				final byte kind = options[i];
				if (kind > 1) {
					if (kind == 8) {
						i += 2;
						if ((i + 7) < options.length) {
							Utilities.writeIntToBytes(header.getmStampSender(), options, i);
							i += 4;
							Utilities.writeIntToBytes(header.getmStampReply(), options, i);
						}
						break;
					} else if ((i + 1) < options.length) {
						final byte len = options[i + 1];
						i = i + len - 1;
					}
				}
			}
			if (options.length > 0) {
				System.arraycopy(options, 0, buffer, 20, options.length);
			}
		}

		return buffer;
	}

	public static tcpHeader createTCPHeader(@NonNull ByteBuffer stream) throws HeaderExcept {
		if(stream.remaining() < 20) {
			throw new HeaderExcept("Not enough space");
		}

		final int sourcePort = stream.getShort() & 0xFFFF;
		final int destPort = stream.getShort() & 0xFFFF;
		final long sequenceNumber = stream.getInt();
		final long ackNumber = stream.getInt();
		final int dataOffsetAndNs = stream.get();

		final int dataOffset = (dataOffsetAndNs & 0xF0) >> 4;
		if(stream.remaining() < (dataOffset - 5) * 4) {
			throw new HeaderExcept("Invalid given start pos");
		}
		
		final boolean isNs = (dataOffsetAndNs & 0x1) > 0x0;
		final int tcpFlag = stream.get();
		final int windowSize = stream.getShort();
		final int checksum = stream.getShort();
		final int urgentPointer = stream.getShort();

		final tcpHeader header = new tcpHeader(sourcePort, destPort, sequenceNumber, ackNumber, dataOffset, isNs, tcpFlag, windowSize, checksum, urgentPointer);
		if (dataOffset > 5) {
			handleTcpOptions(header, stream, dataOffset * 4 - 20);
		}

		return header;
	}

	private static final int mEoO = 0;
	private static final int mNOP = 1;
	private static final int mMaxSegment = 2;
	private static final int mWindowScale = 3;
	private static final int mSACK = 4;
	private static final int mTime = 8;

	private static void handleTcpOptions(@NonNull tcpHeader header, @NonNull ByteBuffer packet, int optionsSize) {
		int index = 0;

		while (index < optionsSize) {
			final byte optionKind = packet.get();
			index++;

			if (optionKind == mEoO || optionKind == mNOP) {
				continue;
			}

			final byte size = packet.get();
			index++;

			switch (optionKind) {
				case mMaxSegment:
					header.setmMaxSeg(packet.getShort());
					index += 2;
					break;
				case mWindowScale:
					header.setmWindowScale(packet.get());
					index++;
					break;
				case mSACK:
					header.setmSACKAllowed(true);
					break;
				case mTime:
					header.setmStampSender(packet.getInt());
					header.setmStampReply(packet.getInt());
					index += 8;
					break;
				default:
					skipRemainingOptions(packet, size);
					index = index + size - 2;
					break;
			}
		}
	}

	private static void skipRemainingOptions(@NonNull ByteBuffer packet, int size) {
		for (int i = 2; i < size; i++) {
			packet.get();
		}
	}
}
