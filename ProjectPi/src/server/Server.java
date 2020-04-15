package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import helper.Transfer;

public class Server {
	private static final int maxFileNameLength = 100;
	private static final double pktLossProb = 0.01;

	private DatagramSocket socket;

	public Server(int port) throws IOException {
		socket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
		broadcast(socket);
	}

	public static void main(String[] args) {
		int port = 9999;
		try {
			(new Server(port)).service();
		} catch (SocketException e) {
			System.out.println("Socket error: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("I/O error: " + e.getMessage());
		}
	}

	private static void broadcast(DatagramSocket socket) throws IOException {
		socket.setBroadcast(true);
		boolean connected = false;
		while (!connected) {
			DatagramPacket pkt = new DatagramPacket(new byte[1], 1);
			socket.receive(pkt);
			if (pkt.getData()[0] == (byte) 99) {
				DatagramPacket resPkt = new DatagramPacket(new byte[] { 100 }, 1, pkt.getAddress(), pkt.getPort());
				socket.send(resPkt);
				connected = true;
				socket.setBroadcast(false);
			}
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
				if (file.exists()) {
					Transfer.sendFile(file, clientAddress, clientPort, socket, pktLossProb);
				}
				break;
			case ('u'):
				// Client wants to upload the file named fileName
				String filePath = System.getProperty("user.dir") + File.separator + fileName;
				Transfer.receiveFile(filePath, socket, pktLossProb);
				break;
			case ('r'):
				// Client wants to remove the file named fileName
				File delFile = new File(fileName);
				delFile.delete();
				break;
			case ('l'):
				// Client wants a list of files
				Transfer.sendFile(listFiles(), clientAddress, clientPort, socket, pktLossProb);
				// delete the temp file on the server (not when running server locally!)
//				File tempFile = new File("listFilesTemp.txt");
//				tempFile.delete();
				break;
			case ('q'):
				// Client wants to quit the program
				quit = true;
				break;
			default:
				// Bit error in very first byte or unexpected error, don't do anything
				break;
			}
		}
		shutdown();
	}

	private File listFiles() throws IOException {
		List<String> results = new ArrayList<String>();

		File[] files = new File(System.getProperty("user.dir")).listFiles();

		File listFiles = new File("listFilesTemp.txt");

		for (File f : files) {
			if (f.isFile() && !(f.getName().contentEquals("listFilesTemp.txt"))) {
				results.add(f.getName());
			}
		}

		FileWriter writer = new FileWriter("listFilesTemp.txt");
		for (String str : results) {
			writer.write(str + System.lineSeparator());
		}
		writer.close();

		return listFiles;
	}

	private void shutdown() {
		socket.close();
		System.exit(0);
	}
}
