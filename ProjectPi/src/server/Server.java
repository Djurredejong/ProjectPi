package server;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

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

//		File file = new File("tiny.pdf");
//		File file = new File("medium.pdf");
		File file = new File("large.pdf");

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

	public void sendFile(File file, InetAddress address, int port) throws IOException {
		byte[] bytesFile = Converter.fileToPacketByteArray(file);
		int off = 0;
		int seqNr = 0;

		System.out.println(bytesFile.length);
		while (off < bytesFile.length) {

			DatagramPacket pkt = new DatagramPacket(bytesFile, off, Math.min(pktSize, (bytesFile.length - off)),
					address, port);
			socket.send(pkt);

			seqNr++;
			System.out.println("Sent packet " + seqNr + " of length " + pkt.getLength());

			off += pktSize;

			// receive acknowledgement or resend the packet after timeout
			DatagramPacket ack = new DatagramPacket(new byte[1], 1);

			socket.setSoTimeout(100);
			try {
				socket.receive(ack);
			} catch (SocketTimeoutException e) {
				System.out.println("Resending packet " + seqNr + " after socket timeout");
				socket.send(pkt);
			}

		}
	}

	private void shutdown() {
		socket.close();
	}
}
