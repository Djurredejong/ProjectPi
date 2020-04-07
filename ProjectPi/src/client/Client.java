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
	/** according to my protocol: 2B seqNr, 2B checksum, 512B data */
	private static final int pktSize = 516;

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

		String fileName = "tryEmptyFile.txt";
		receiveFile(System.getProperty("user.dir") + File.separator + fileName);
	}

	private void receiveFile(String pathName) throws IOException {
		// TODO recFileLength to be received as first response to request
		int recFileLength = (3961 + 4 * (3961 % 512 + 1));
		byte[] recFileBytes = new byte[recFileLength];
		byte[] buf = new byte[pktSize];

		int off = 0;
		while (off < recFileLength) {

			DatagramPacket pkt = new DatagramPacket(buf, pktSize);
			socket.receive(pkt);

			for (int i = 0; i < pkt.getLength(); i++) {
				// TODO calculate starting position for the data based on seqNr
				recFileBytes[i + off] = pkt.getData()[i];
			}
			off += pktSize;
		}

		Converter.byteArrayToFile(recFileBytes, pathName);
	}
}
