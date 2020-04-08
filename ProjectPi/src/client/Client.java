package client;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

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

//		String fileName = "tiny.pdf";
		String fileName = "medium.pdf";
		receiveFile(System.getProperty("user.dir") + File.separator + "temp" + File.separator + fileName);
	}

	private void receiveFile(String pathName) throws IOException {
		// TODO recFileLength to be received as first response to request
		// tiny length
		// int recFileLength = 24286;
		// medium length
		int recFileLength = 475231;
		// large length
		// int recFileLength = 31498458;
		byte[] recFileBytes = new byte[recFileLength];
		byte[] buf = new byte[pktSize];

		int off = 0;
		int i = 0;
		ArrayList<Integer> recPkt = new ArrayList<Integer>();
		while (off < recFileLength) {

			DatagramPacket pkt = new DatagramPacket(buf, pktSize);
			System.out.println("off = " + off);
			socket.receive(pkt);

			int seqNr = (pkt.getData()[0] << 8) | (pkt.getData()[1] & 0xFF);
			System.out.println("received packet number " + seqNr + " of length " + pkt.getLength());

			recPkt.add(seqNr);

			System.arraycopy(pkt.getData(), headSize, recFileBytes, off, Math.min(mtu, (recFileLength - off)));
			off += mtu;

			System.out.println("number of received packets= " + recPkt.size());
		}

		System.out.println("The file has been received!");
		Converter.byteArrayToFile(recFileBytes, pathName);
	}
}
