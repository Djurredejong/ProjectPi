package server;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import helper.Transfer;

public class Server {

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

//		File file = new File("tiny.pdf");
//		File file = new File("medium.pdf");
		File file = new File("large.pdf");

		while (true) {
			DatagramPacket request = new DatagramPacket(new byte[1], 1);
			socket.receive(request);

			InetAddress clientAddress = request.getAddress();
			int clientPort = request.getPort();

			Transfer.sendFile(file, clientAddress, clientPort, socket, 0);

			shutdown();
		}

	}

	private void shutdown() {
		socket.close();
	}
}
