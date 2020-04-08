package client;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import helper.Converter;

public class Client {

	private DatagramSocket socket;
	/**
	 * the "header" consists of a 2B seqNr and a 2B checksum for data integrity
	 */
	private static final int headSize = 4;
	private static final int mtu = 512;
	private static final int pktSize = headSize + mtu;

	public Client() throws SocketException {
		socket = new DatagramSocket();
	}

	public static void main(String[] args) {
		System.out.println("Hello");
		try {
			Client client = new Client();
			int port = 9999;
			client.start(port);
		} catch (SocketException e) {
			System.out.println("Socket error: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("I/O error: " + e.getMessage());
		}
	}

	public void start(int port) throws IOException {

		InetAddress address = InetAddress.getByName(null);

		DatagramPacket request = new DatagramPacket(new byte[1], 1, address, port);

		socket.send(request);
		System.out.println("request sent");

		// wait for the response on your request to receive a file
		InetAddress servAddress = null;
		int servPort = 0;
		int recFileLength = 0;

//		String fileName = "tiny.pdf";
//		String fileName = "medium.pdf";
		String fileName = "large.pdf";
		String filePath = System.getProperty("user.dir") + File.separator + "temp" + File.separator + fileName;
		receiveFile(filePath, recFileLength, servAddress, servPort);
	}

	private void receiveFile(String pathName, int recFileLength, InetAddress address, int port) throws IOException {
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
			System.out.println("off = " + off);
			socket.receive(pkt);

			seqNr = (pkt.getData()[0] << 8) | (pkt.getData()[1] & 0xFF);
			System.out.println("Received packet number " + seqNr + " of length " + pkt.getLength());

			if (seqNr == prevSeqNr) {
				// in case the packet is the same as the last one we received (which happens in
				// case the last ack got lost), do not copy the data of the received packet into
				// recFileBytes (but do resend the ack)
			} else if (seqNr == prevSeqNr + 1) {
				System.arraycopy(pkt.getData(), headSize, recFileBytes, off, Math.min(mtu, (recFileLength - off)));
				off += mtu;
			} else {
				System.out.println("Error: very unexpected sequence number");
				// TODO define own exceptions like ExitProgram
			}

			address = pkt.getAddress();
			port = pkt.getPort();
			// send acknowledgement
			DatagramPacket ack = new DatagramPacket(new byte[1], 1, address, port);
			socket.send(ack);
		}

		System.out.println("The file has been received!");
		Converter.byteArrayToFile(recFileBytes, pathName);
	}
}
