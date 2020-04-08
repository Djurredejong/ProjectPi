package helper;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class Transfer {
	/**
	 * the "header" consists of a 2B seqNr and a 2B checksum for data integrity
	 */
	private static final int headSize = 4;
	private static final int mtu = 512;
	private static final int pktSize = headSize + mtu;

	/**
	 * Sends a file, receiveFile should be called by another program before calling
	 * sendFile
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
		int seqNr = -1;

		int resendCount = 0;

		System.out.println(bytesFile.length);
		while (off < bytesFile.length) {

			DatagramPacket pkt = new DatagramPacket(bytesFile, off, Math.min(pktSize, (bytesFile.length - off)),
					address, port);
			sendPacket(pkt, socket, pktLossProb);

			seqNr++;
			System.out.println("Sent packet with sequence number " + seqNr % 32768 + " of length " + pkt.getLength());
			off += pktSize;

			// receive acknowledgement or resend the packet after timeout
			DatagramPacket ack = new DatagramPacket(new byte[1], 1);

			socket.setSoTimeout(100);
			try {
				socket.receive(ack);
			} catch (SocketTimeoutException e) {
				System.out.println("Resending packet " + seqNr % 32768 + " after socket timeout");
				sendPacket(pkt, socket, pktLossProb);
				resendCount++;
			}

		}
		System.out.println(resendCount + " times a packet was resend");
	}

//	public static void receiveFile(String pathName, int recFileLength, InetAddress address, int port,
//			DatagramSocket socket) throws IOException {
//		boolean withSenderInfo = true;
//	}

	/**
	 * Receives a file, receiveFile should be called before another program calls
	 * sendFile
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
		// tiny length
		// recFileLength = 24286;
		// medium length
		// recFileLength = 475231;
		// large length
		recFileLength = 31498458;
		byte[] recFileBytes = new byte[recFileLength];
		byte[] buf = new byte[pktSize];

		int off = 0;
		int prevSeqNr = -1;
		int seqNr = -1;

//		ArrayList<Integer> recPkt = new ArrayList<Integer>();
		while (off < recFileLength) {
			prevSeqNr = seqNr;

			DatagramPacket pkt = new DatagramPacket(buf, pktSize);
			socket.receive(pkt);

			seqNr = (pkt.getData()[0] << 8) | (pkt.getData()[1] & 0xFF);
			System.out.println("Received packet with sequence number " + seqNr + " of length " + pkt.getLength());

			if (seqNr == prevSeqNr) {
				// in case the packet is the same as the last one we received (which happens in
				// case the last ack got lost), do not copy the data of the received packet into
				// recFileBytes (but do resend the ack)
			} else if (seqNr == prevSeqNr + 1 || (prevSeqNr == 32767 && seqNr == 0)) {
				System.arraycopy(pkt.getData(), headSize, recFileBytes, off, Math.min(mtu, (recFileLength - off)));
				off += mtu;
			} else {
				System.out.println("Error: very unexpected sequence number");
				// TODO define own exceptions like ExitProgram
			}

			InetAddress address = pkt.getAddress();
			int port = pkt.getPort();
			// send acknowledgement
			DatagramPacket ack = new DatagramPacket(new byte[1], 1, address, port);
			sendPacket(ack, socket, pktLossProb);
		}

		System.out.println("The file has been received!");
		Converter.byteArrayToFile(recFileBytes, pathName);
	}

	/**
	 * Sends a DatagramPacket via the provided socket, or not (simulating packet
	 * loss)
	 * 
	 * @param pkt         The packet to be send
	 * @param socket      The socket to send the packet via
	 * @param pktLossProb The probability that the packet gets lost
	 */
	public static void sendPacket(DatagramPacket pkt, DatagramSocket socket, double pktLossProb) throws IOException {
		if (pktLossProb == 0) {
			socket.send(pkt);
		} else {
			if ((int) (Math.random()) > pktLossProb) {
				socket.send(pkt);
			}
		}
	}

}
