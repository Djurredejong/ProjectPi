package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

import helper.Transfer;

public class Client {
	private static final int maxFileNameLength = 100;
	private static final double pktLossProb = 0.01;

	private DatagramSocket socket;
	private InetAddress address;
	private int port;

	private TUI tui;

	public Client() throws SocketException {
		socket = new DatagramSocket();
		tui = new TUI(this);
	}

	public static void main(String[] args) {
		try {
			int port = 9999;
			(new Client()).start(port);
		} catch (SocketException e) {
			System.out.println("Socket error: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("I/O error: " + e.getMessage());
		}
	}

	public void start(int port) throws IOException {
		this.address = InetAddress.getByName(null);
		this.port = port;
		tui.start();
	}

	public void download(String fileName) throws IOException {
		sendRequest("d " + fileName);
//		System.out.println(fileName + " will now be downloaded!");
//		String filePath = System.getProperty("user.dir") + File.separator + "testDownload" + File.separator + fileName;
		String filePath = System.getProperty("user.dir") + File.separator + fileName;
		Transfer.receiveFile(filePath, socket, pktLossProb);
		System.out.println(fileName + " has been downloaded!");
	}

	public void upload(String fileName) throws IOException {
		sendRequest("u " + fileName);
//		System.out.println(fileName + " will now be uploaded!");
		File file = new File(fileName);
		Transfer.sendFile(file, address, port, socket, pktLossProb);
		System.out.println(fileName + " has been uploaded!");
	}

	public void remove(String fileName) throws IOException {
		sendRequest("r " + fileName);
//		System.out.println(fileName + " will now be removed!");
	}

	public void listFiles() throws IOException {
		sendRequest("l");
//		String tempFilePath = System.getProperty("user.dir") + File.separator + "testList" + File.separator
//				+ "listFilesTemp.txt";
		String tempFilePath = System.getProperty("user.dir") + File.separator + "listFilesTemp.txt";
		Transfer.receiveFile(tempFilePath, socket, pktLossProb);
		BufferedReader br = new BufferedReader(new FileReader("listFilesTemp.txt"));
		String fileName;
		while ((fileName = br.readLine()) != null) {
			System.out.println(fileName);
		}
		br.close();
		File tempFile = new File("listFilesTemp.txt");
		tempFile.delete();
	}

	public void quit() throws IOException {
		sendRequest("q");
		System.out.println("Goodbye!");
		shutdown();
	}

	private void sendRequest(String request) throws IOException {
		byte[] reqBytes = new byte[maxFileNameLength + 2];

		byte[] fileNameBytes = new byte[request.length()];
		fileNameBytes = request.getBytes(StandardCharsets.UTF_8);

		System.arraycopy(fileNameBytes, 0, reqBytes, 0, request.length());
		for (int i = request.length(); i < reqBytes.length; i++) {
			reqBytes[i] = 0;
		}

		DatagramPacket reqPkt = new DatagramPacket(reqBytes, reqBytes.length, address, port);
		socket.send(reqPkt);
	}

	private void shutdown() {
		socket.close();
		System.exit(0);
	}

	/**
	 * TUI needs to know the max. lenght of the name of a file
	 */
	public int getMaxFileNameLength() {
		return maxFileNameLength;
	}

}
