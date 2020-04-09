package helper;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class Transfer {
	/**
	 * The "header" consists of a 2B seqNr and an unused 2B (perhaps to be used as
	 * checksum for data integrity)
	 */
	private static final int headSize = 4;
	private static final int mtu = 512;
	private static final int pktSize = headSize + mtu;

	/**
	 * receiveFile should be called by another program before calling sendFile
	 * 
	 * @param file        The file to be send
	 * @param address     The address to send to
	 * @param port        The port to send on
	 * @param socket      The socket to send via
	 * @param pktLossProb Probability that a packet being send by the sender of the
	 *                    file gets lost. Set to 0 if you do not want to simulate
	 *                    packet loss on the packets being send by the sender of the
	 *                    file
	 */
	public static void sendFile(File file, InetAddress address, int port, DatagramSocket socket, double pktLossProb)
			throws IOException {

		byte[] bytesFile = Converter.fileToPacketByteArray(file);

		int off = 0;
		byte[] bytesSeq = new byte[2];

		while (off < bytesFile.length) {

			DatagramPacket pkt = new DatagramPacket(bytesFile, off, Math.min(pktSize, (bytesFile.length - off)),
					address, port);
			sendPacket(pkt, socket, pktLossProb);

			bytesSeq[0] = bytesFile[off];
			bytesSeq[1] = bytesFile[off + 1];
//			int seqNr = twoBytesToInt(bytesSeq);
//			System.out.println("Sent packet with sequence number " + seqNr + " of length " + pkt.getLength());
			if (off > (bytesFile.length - pktSize)) {
				// When sending the very last packet and the ack for it gets lost, prevent
				// getting stuck in an infinite loop!
				receiveAck(pkt, socket, bytesSeq, pktLossProb);
			} else {
				receiveAck(pkt, socket, bytesSeq, pktLossProb);
			}
			off += pktSize;
		}

	}

	/**
	 * Receive correct acknowledgement or retransmit the packet after timeout
	 */
	public static void receiveAck(DatagramPacket pkt, DatagramSocket socket, byte[] bytesSeq, double pktLossProb)
			throws IOException {
		boolean recAck = false;
		byte[] bytesAck = new byte[2];
		DatagramPacket ack = new DatagramPacket(new byte[2], 2);
		try {
			socket.setSoTimeout(100);
			socket.receive(ack);
			bytesAck[0] = ack.getData()[0];
			bytesAck[1] = ack.getData()[1];
//			System.out.println("Received ack with sequence number " + twoBytesToInt(bytesAck));
			recAck = true;
		} catch (SocketTimeoutException e) {
			recAck = false;
		}
		if (recAck && Arrays.equals(bytesAck, bytesSeq)) {
			// All good!
		} else {
			// Keep retransmitting and waiting for the correct ack until the transfer
			// succeeds
			sendPacket(pkt, socket, pktLossProb);
			receiveAck(pkt, socket, bytesSeq, pktLossProb);
		}
	}

	/**
	 * receiveFile should be called before another program calls sendFile
	 * 
	 * @param pathName      The path (incl. filename and extension) where the file
	 *                      should be stored
	 * @param recFileLength The lenght of the file to be received
	 * @param socket        The socket the file is being send via
	 * @param pktLossProb   Probability that a packet (ack) being send by the
	 *                      receiver of the file gets lost. Set to 0 if you do not
	 *                      want to simulate packet loss on the packets (acks) being
	 *                      send by the receiver of the file
	 */
	public static void receiveFile(String pathName, int recFileLength, DatagramSocket socket, double pktLossProb)
			throws IOException {

		byte[] recFileBytes = new byte[recFileLength];
		byte[] buf = new byte[pktSize];

		int off = 0;
		int prevSeqNr = -1;
		int seqNr = -1;

		while (off < recFileLength) {
			prevSeqNr = seqNr;

			DatagramPacket pkt = new DatagramPacket(buf, pktSize);
			socket.setSoTimeout(0);
			socket.receive(pkt);

			seqNr = (pkt.getData()[0] << 8) | (pkt.getData()[1] & 0xFF);
//			System.out.println("Received packet with sequence number " + seqNr + " of length " + pkt.getLength());

			InetAddress address = pkt.getAddress();
			int port = pkt.getPort();
			if (seqNr == prevSeqNr) {
				// In case the packet is the same as the last one we received, we do not copy
				// the data of the received packet into recFileBytes
			} else if (seqNr == prevSeqNr + 1 || (prevSeqNr == 32767 && seqNr == 0)) {
				// In case the packet is not the same as the last one we received, we copy the
				// data of the received packet into recFileBytes
				System.arraycopy(pkt.getData(), headSize, recFileBytes, off, Math.min(mtu, (recFileLength - off)));
				off += mtu;
			} else {
				System.out.println("Error: very unexpected sequence number");
			}

			// Send seqNr back as acknowledgement
			byte[] bytesAck = new byte[2];
			bytesAck[0] = pkt.getData()[0];
			bytesAck[1] = pkt.getData()[1];
			DatagramPacket ack = new DatagramPacket(bytesAck, 2, address, port);
			sendPacket(ack, socket, pktLossProb);
//			System.out.println("Sent ack with sequence number " + twoBytesToInt(bytesAck));
//			System.out.println("The offset is " + off);
		}
		System.out.println("The file has been received!");
		Converter.byteArrayToFile(recFileBytes, pathName);
	}

	/**
	 * Transmits a DatagramPacket via the provided socket, or not (simulating packet
	 * loss)
	 */
	public static void sendPacket(DatagramPacket pkt, DatagramSocket socket, double pktLossProb) throws IOException {
		if (pktLossProb == 0) {
			socket.send(pkt);
		} else {
			if (Math.random() > pktLossProb) {
				socket.send(pkt);
			} else {
				// The packet got lost!
			}
		}
	}

	public static int twoBytesToInt(byte[] byteArray) {
		return (byteArray[0] << 8 & 0xFF00) | (byteArray[1] & 0xFF);
	}

}
