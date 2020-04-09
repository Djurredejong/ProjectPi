package client;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

import helper.Transfer;

public class Client {
	private static final int maxFileNameLength = 100;

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
//		String fileName = "tiny.pdf";
//		int recFileLength = 24286;
//		String fileName = "medium.pdf";
//		int recFileLength = 475231;
//		String fileName = "large.pdf";
//		int recFileLength = 31498458;
		String fileName = "picture.png";
		int recFileLength = 141270;

		InetAddress address = InetAddress.getByName(null);

		System.out.println("What do you want to do?");
		String response = "d " + fileName;

		byte[] reqBytes = new byte[maxFileNameLength + 2];

		byte[] responseBytes = new byte[response.length()];
		responseBytes = response.getBytes(StandardCharsets.UTF_8);

		System.arraycopy(responseBytes, 0, reqBytes, 0, response.length());
		for (int i = response.length(); i < reqBytes.length; i++) {
			reqBytes[i] = 0;
		}
		System.out.println(reqBytes[0]);

		DatagramPacket reqPkt = new DatagramPacket(reqBytes, reqBytes.length, address, port);

		socket.send(reqPkt);
		System.out.println("request sent");

		// wait for the response on your request to receive a file; the file length will
		// be in this response, so that receiveFile can be called

		String filePath = System.getProperty("user.dir") + File.separator + "temp" + File.separator + fileName;
		Transfer.receiveFile(filePath, socket, 0.05);
	}

}
