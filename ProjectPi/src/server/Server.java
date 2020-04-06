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

	public Server(int port) throws SocketException {
		socket = new DatagramSocket(port);
	}

	public static void main(String[] args) {
		try {
			Server server = new Server(9999);
			server.service();
		} catch (SocketException ex) {
			System.out.println("Socket error: " + ex.getMessage());
		} catch (IOException ex) {
			System.out.println("I/O error: " + ex.getMessage());
		}
	}

//	private DatagramPacket sendFile() throws IOException {
//		File file = new File("Quotes.txt");
//		byte[] bytesFile = readBytesFromFile(file);
//		new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
//	}

	private void service() throws IOException {

		File file = new File("Empty.pdf");
		byte[] tryout = Converter.fileToBytes(file);
		for (int i = 0; i < tryout.length; i++) {
			System.out.println(tryout[i]);
		}
		System.out.println(tryout.length);

		shutdown();

		while (true) {
			DatagramPacket request = new DatagramPacket(new byte[1], 1);
			socket.receive(request);

			InetAddress clientAddress = request.getAddress();
			int clientPort = request.getPort();

//			File file = new File("Quotes.txt");
			byte[] bytesFile = Converter.fileToBytes(file);
			DatagramPacket response = new DatagramPacket(bytesFile, bytesFile.length, clientAddress, clientPort);

			socket.send(response);
		}
	}

	private void shutdown() {
		socket.close();
	}
}
