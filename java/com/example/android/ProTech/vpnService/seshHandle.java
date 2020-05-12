package com.example.android.ProTech.vpnService;

import android.support.annotation.NonNull;

import com.example.android.ProTech.vpnService.packet.PacketFactory;
import com.example.android.ProTech.vpnService.packet.PacketHeader;
import com.example.android.ProTech.vpnService.socket.SocketData;
import com.example.android.ProTech.vpnService.transport.headerInterface;
import com.example.android.ProTech.vpnService.transport.tcp.HeaderExcept;
import com.example.android.ProTech.vpnService.transport.tcp.tcpFactory;
import com.example.android.ProTech.vpnService.transport.tcp.tcpHeader;
import com.example.android.ProTech.vpnService.transport.udp.udpFactory;
import com.example.android.ProTech.vpnService.transport.udp.udpHeader;
import com.example.android.ProTech.vpnService.util.Utilities;

import java.io.IOException;
import java.nio.ByteBuffer;


class seshHandle {
	private static final seshHandle mHandler = new seshHandle();
	private CWInterface mWriter;
	private SocketData mData;
	//	private final String queryString = "http://free.ipwhois.io/xml/";
	private boolean mVulnIP = false;

	public void resetVuln() { mVulnIP = false; }
	public boolean getVuln() { return mVulnIP; }

	public static seshHandle getInstance(){
		return mHandler;
	}

	private seshHandle(){
		mData = SocketData.getmInst();
	}

	void setmWriter(CWInterface mWriter){
		this.mWriter = mWriter;
	}

	private void handleUDPPacket(ByteBuffer clientPacketData, PacketHeader ipHeader, udpHeader udpheader){
		sesh sesh = sashManage.INSTANCE.getSession(ipHeader.getmDestIP(), udpheader.getmDestPrt(),
				ipHeader.getmSrcIP(), udpheader.getmSrcPrt());

		if(sesh == null){
			sesh = sashManage.INSTANCE.createNewUDPSession(ipHeader.getmDestIP(), udpheader.getmDestPrt(),
					ipHeader.getmSrcIP(), udpheader.getmSrcPrt());
		}

		if(sesh == null){
			return;
		}

		sesh.setmIpHeaderLast(ipHeader);
		sesh.setmUdpHeaderLast(udpheader);
		int len = sashManage.INSTANCE.addClientData(clientPacketData, sesh);
		sesh.setmOutReady(true);
		sashManage.INSTANCE.keepSessionAlive(sesh);
	}

	private void handleTCPPacket(ByteBuffer clientPacketData, PacketHeader ipHeader, tcpHeader tcpheader) {
		int dataLength = clientPacketData.limit() - clientPacketData.position();
		int sourceIP = ipHeader.getmSrcIP();
		int destinationIP = ipHeader.getmDestIP();
		int sourcePort = tcpheader.getmSrcPrt();
		int destinationPort = tcpheader.getmDestPrt();
		String eIP = Utilities.intToIPAddress(destinationIP);

		if (Utilities.checkVulnIP(eIP) || eIP.startsWith("104.") ) {
			mVulnIP = true;
		}

		if(tcpheader.isSYN()) {
			replySynAck(ipHeader,tcpheader);
		} else if(tcpheader.isACK()) {
			String key = sashManage.INSTANCE.createKey(destinationIP, destinationPort, sourceIP, sourcePort);
			sesh sesh = sashManage.INSTANCE.getSessionByKey(key);

			if(sesh == null) {
				if (tcpheader.isFIN()) {
					sendLastAck(ipHeader, tcpheader);
				} else if (!tcpheader.isRST()) {
					sendRstPacket(ipHeader, tcpheader, dataLength);
				}
				else {
				}
				return;
			}

			sesh.setmIpHeaderLast(ipHeader);
			sesh.setmTcpHeaderLast(tcpheader);

			if(dataLength > 0) {
			if(sesh.getmRecSeq() == 0 || tcpheader.getmSeqNum() >= sesh.getmRecSeq()) {
					int addedLength = sashManage.INSTANCE.addClientData(clientPacketData, sesh);
					sendAck(ipHeader, tcpheader, addedLength, sesh);
				} else {
					sendAckForDisorder(ipHeader, tcpheader, dataLength);
				}
			} else {
				acceptAck(tcpheader, sesh);

				if(sesh.ismEndCon()){
					sendFinAck(ipHeader, tcpheader, sesh);
				}else if(sesh.ismToFin() && !tcpheader.isFIN()){
					sashManage.INSTANCE.closeSession(destinationIP, destinationPort, sourceIP, sourcePort);
				}
			}

			if(tcpheader.isPSH()){
				pushDataToDestination(sesh, tcpheader);
			} else if(tcpheader.isFIN()){
				ackFinAck(ipHeader, tcpheader, sesh);
			} else if(tcpheader.isRST()){
				resetConnection(ipHeader, tcpheader);
			}

			if(!sesh.isClientWindowFull() && !sesh.ismAbortCon()){
				sashManage.INSTANCE.keepSessionAlive(sesh);
			}
		} else if(tcpheader.isFIN()){
			sesh sesh = sashManage.INSTANCE.getSession(destinationIP, destinationPort, sourceIP, sourcePort);
			if(sesh == null)
				ackFinAck(ipHeader, tcpheader, null);
			else
				sashManage.INSTANCE.keepSessionAlive(sesh);

		} else if(tcpheader.isRST()){
			resetConnection(ipHeader, tcpheader);
		} else {
		}
	}

	void handlePacket(@NonNull ByteBuffer stream) throws HeaderExcept {
		final byte[] rawPacket = new byte[stream.limit()];
		stream.get(rawPacket, 0, stream.limit());
		mData.addData(rawPacket);
		stream.rewind();

		final PacketHeader ipHeader = PacketFactory.createIPv4Header(stream);

		final headerInterface transportHeader;
		if(ipHeader.getmPrtcl() == 6) {
			transportHeader = tcpFactory.createTCPHeader(stream);
		} else if(ipHeader.getmPrtcl() == 17) {
			transportHeader = udpFactory.createUDPHeader(stream);
		} else {
			return;
		}

		if (transportHeader instanceof tcpHeader) {
			handleTCPPacket(stream, ipHeader, (tcpHeader) transportHeader);
		} else if (ipHeader.getmPrtcl() == 17){
			handleUDPPacket(stream, ipHeader, (udpHeader) transportHeader);
		}
	}

	private void sendRstPacket(PacketHeader ip, tcpHeader tcp, int dataLength){
		byte[] data = tcpFactory.createRstData(ip, tcp, dataLength);
		try {
			mWriter.write(data);
			mData.addData(data);
		} catch (IOException e) {
		}
	}

	private void sendLastAck(PacketHeader ip, tcpHeader tcp){
		byte[] data = tcpFactory.createResponseAckData(ip, tcp, tcp.getmSeqNum()+1);
		try {
			mWriter.write(data);
			mData.addData(data);
		} catch (IOException e) {
		}
	}

	private void ackFinAck(PacketHeader ip, tcpHeader tcp, sesh sesh){
		long ack = tcp.getmSeqNum() + 1;
		long seq = tcp.getmAckNum();
		byte[] data = tcpFactory.createFinAckData(ip, tcp, ack, seq, true, true);
		try {
			mWriter.write(data);
			mData.addData(data);
			if(sesh != null){
				sesh.getSelectionKey().cancel();
				sashManage.INSTANCE.closeSession(sesh);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendFinAck(PacketHeader ip, tcpHeader tcp, sesh sesh){
		final long ack = tcp.getmSeqNum();
		final long seq = tcp.getmAckNum();
		final byte[] data = tcpFactory.createFinAckData(ip, tcp, ack, seq,true,false);
		final ByteBuffer stream = ByteBuffer.wrap(data);
		try {
			mWriter.write(data);
			mData.addData(data);
			PacketHeader vpnip = null;
			try {
				vpnip = PacketFactory.createIPv4Header(stream);
			} catch (HeaderExcept e) {
				e.printStackTrace();
			}

			tcpHeader vpntcp = null;
			try {
				if (vpnip != null)
					vpntcp = tcpFactory.createTCPHeader(stream);
			} catch (HeaderExcept e) {
				e.printStackTrace();
			}

			if(vpnip != null && vpntcp != null){
				String sout = Utilities.getOutput(vpnip, vpntcp, data);
			}

		} catch (IOException e) {
		}
		sesh.setmSndNxt(seq + 1);
		sesh.setmEndCon(false);
	}

	private void pushDataToDestination(sesh sesh, tcpHeader tcp){
		sesh.setmOutReady(true);
		sesh.setmStampReply(tcp.getmStampSender());
		sesh.setmStampSndr((int) System.currentTimeMillis());

	}

	private void sendAck(PacketHeader ipheader, tcpHeader tcpheader, int acceptedDataLength, sesh sesh){
		long acknumber = sesh.getmRecSeq() + acceptedDataLength;
		sesh.setmRecSeq(acknumber);
		byte[] data = tcpFactory.createResponseAckData(ipheader, tcpheader, acknumber);
		try {
			mWriter.write(data);
			mData.addData(data);
		} catch (IOException e) {
		}
	}

	private void sendAckForDisorder(PacketHeader ipHeader, tcpHeader tcpheader, int acceptedDataLength) {
		long ackNumber = tcpheader.getmSeqNum() + acceptedDataLength;
		byte[] data = tcpFactory.createResponseAckData(ipHeader, tcpheader, ackNumber);
		try {
			mWriter.write(data);
			mData.addData(data);
		} catch (IOException e) {
		}
	}

	private void acceptAck(tcpHeader tcpHeader, sesh sesh){
		boolean isCorrupted = Utilities.isPacketCorrupted(tcpHeader);
		sesh.setmCorrupt(isCorrupted);
		if(isCorrupted){
		}
		if(tcpHeader.getmAckNum() > sesh.getmUnACK() ||
				tcpHeader.getmAckNum() == sesh.getmSndNxt()){
			sesh.setAcked(true);
			
			if(tcpHeader.getmWindowSize() > 0){
				sesh.setSendWindowSizeAndScale(tcpHeader.getmWindowSize(), sesh.getmSndWndwScl());
			}
			sesh.setmUnACK(tcpHeader.getmAckNum());
			sesh.setmRecSeq(tcpHeader.getmSeqNum());
			sesh.setmStampReply(tcpHeader.getmStampSender());
			sesh.setmStampSndr((int) System.currentTimeMillis());
		} else {
			sesh.setAcked(false);
		}
	}

	private void resetConnection(PacketHeader ip, tcpHeader tcp){
		sesh sesh = sashManage.INSTANCE.getSession(ip.getmDestIP(), tcp.getmDestPrt(),
				ip.getmSrcIP(), tcp.getmSrcPrt());
		if(sesh != null){
			sesh.setmAbortCon(true);
		}
	}


	private void replySynAck(PacketHeader ip, tcpHeader tcp){
		ip.setmID(0);
		Packet packet = tcpFactory.createSynAckPacketData(ip, tcp);
		
		tcpHeader tcpheader = (tcpHeader) packet.getmTransHeader();
		
		sesh sesh = sashManage.INSTANCE.createNewSession(ip.getmDestIP(),
				tcp.getmDestPrt(), ip.getmSrcIP(), tcp.getmSrcPrt());
		if(sesh == null)
			return;
		
		int windowScaleFactor = (int) Math.pow(2, tcpheader.getmWindowScale());
		sesh.setSendWindowSizeAndScale(tcpheader.getmWindowSize(), windowScaleFactor);
		sesh.setmMaxSegSize(tcpheader.getmMaxSeg());
		sesh.setmUnACK(tcpheader.getmSeqNum());
		sesh.setmSndNxt(tcpheader.getmSeqNum() + 1);
		sesh.setmRecSeq(tcpheader.getmAckNum());

		try {
			mWriter.write(packet.getmBuffer());
			mData.addData(packet.getmBuffer());
		} catch (IOException e) {
		}
	}
}
