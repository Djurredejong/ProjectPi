package client;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import helper.Transfer;

public class Client {

	private DatagramSocket socket;

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
		int recFileLength = 0;

		String fileName = "tiny.pdf";
//		String fileName = "medium.pdf";
//		String fileName = "large.pdf";
		String filePath = System.getProperty("user.dir") + File.separator + "temp" + File.separator + fileName;
//		Transfer.receiveFile(filePath, recFileLength, servAddress, servPort, socket);
		Transfer.receiveFile(filePath, recFileLength, socket, 0);
	}

}
