package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

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

	public byte[] fileToBytes(File file) throws IOException {

		FileInputStream fileInputStream = null;
		byte[] bytesArray = new byte[(int) file.length()];

		try {
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(bytesArray);
		} catch (IOException e) {
			throw new IOException("Could not read (bytes from) file!");
		} finally {
			fileInputStream.close();
		}
		return bytesArray;
	}

	private void service() throws IOException {

		File file = new File("Empty.txt");
		byte[] tryout = fileToBytes(file);
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
			byte[] bytesFile = fileToBytes(file);
			DatagramPacket response = new DatagramPacket(bytesFile, bytesFile.length, clientAddress, clientPort);

			socket.send(response);
		}
	}

	private void shutdown() {
		socket.close();
	}
}
