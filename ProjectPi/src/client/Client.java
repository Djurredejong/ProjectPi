package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class Client {

	public static void main(String[] args) {

		try {
			InetAddress address = InetAddress.getLocalHost();// InetAddress.getByName(hostname);
			DatagramSocket socket = new DatagramSocket();
			int port = 9999;

			while (true) {

				receiveFile();

				DatagramPacket request = new DatagramPacket(new byte[1], 1, address, port);
				socket.send(request);

				byte[] buffer = new byte[2];
				DatagramPacket response = new DatagramPacket(buffer, buffer.length);
				socket.receive(response);

				String file = new String(buffer, 0, response.getLength());

				System.out.println(file);
				System.out.println();

				Thread.sleep(1000);
			}
			socket.close();

		} catch (SocketTimeoutException e) {
			System.out.println("Timeout error: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Client error: " + e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("Interrupted error: " + e.getMessage());
			e.printStackTrace();
		}

	}

	private static void receiveFile() {
		// length to be received in
		byte[] byteArray = new byte[length];
		int off = 0;
		while (off < bytesFile.length) {

			DatagramPacket pkt = new DatagramPacket(bytesFile, off, pktSize, clientAddress, clientPort);

			socket.send(pkt);

			off += pktSize;
		}
	}
}
