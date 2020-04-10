package server;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

import helper.Transfer;

public class Server {
	private static final int maxFileNameLength = 100;
	private static final double pktLossProb = 0.01;

	private DatagramSocket socket;

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

		byte[] buf = new byte[maxFileNameLength + 2];
		boolean quit = false;

		while (!quit) {
			DatagramPacket reqPkt = new DatagramPacket(buf, maxFileNameLength + 2);
			socket.setSoTimeout(0);
			socket.receive(reqPkt);
			InetAddress clientAddress = reqPkt.getAddress();
			int clientPort = reqPkt.getPort();

			String fileName = null;
			File file = null;
			if (reqPkt.getLength() > 2 && reqPkt.getData()[2] != 0) {
				fileName = new String(reqPkt.getData(), 2, maxFileNameLength, StandardCharsets.UTF_8);
				fileName = fileName.trim();
				file = new File(fileName);
			}

			char reqNr = (char) reqPkt.getData()[0];
			switch (reqNr) {
			case ('d'):
				// Client wants to download the file named fileName
				Transfer.sendFile(file, clientAddress, clientPort, socket, pktLossProb);
				break;
			case ('u'):
				// Client wants to upload the file named fileName
				String filePath = System.getProperty("user.dir") + File.separator + "temp" + File.separator + fileName;
				Transfer.receiveFile(filePath, socket, pktLossProb);
				break;
			case ('r'):
				// Client wants to remove the file named fileName

				break;
			case ('l'):
				// Client wants a list of files

				break;
			case ('q'):
				// Client wants to quit the program
				quit = true;
				break;
			default:
				// Bit error in very first byte, don't do anything
				break;
			}
		}
		shutdown();
	}

	private void shutdown() {
		socket.close();
		System.exit(0);
	}
}
