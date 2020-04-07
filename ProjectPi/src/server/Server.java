package server;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import helper.Converter;

public class Server {

	private DatagramSocket socket;
	/** according to my protocol: 2B seqNr, 2B checksum, 512B data */
	private static final int pktSize = 516;

	public Server(int port) throws SocketException {
		socket = new DatagramSocket(port);
	}

	public static void main(String[] args) {
		System.out.println("The server has been started");
		try {
			int port = 9999;
			Server server = new Server(port);
			server.service();
		} catch (SocketException e) {
			System.out.println("Socket error: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("I/O error: " + e.getMessage());
		}
	}

	public void service() throws IOException {

		File file = new File("Empty.pdf");

		while (true) {
			DatagramPacket request = new DatagramPacket(new byte[1], 1);
			socket.receive(request);

			InetAddress clientAddress = request.getAddress();
			int clientPort = request.getPort();

			sendFile(file, clientAddress, clientPort);

			// close the socket
			shutdown();
		}
	}

	public void sendFile(File file, InetAddress clientAddress, int clientPort) throws IOException {
		byte[] bytesFile = Converter.fileToPacketByteArray(file);
		int off = 0;
		while (off < bytesFile.length) {

			DatagramPacket pkt = new DatagramPacket(bytesFile, pktSize, clientAddress, clientPort);

			socket.send(pkt);

			off += pktSize;
		}
	}

	private void shutdown() {
		socket.close();
	}
}
