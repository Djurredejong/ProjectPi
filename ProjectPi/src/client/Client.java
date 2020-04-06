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

		} catch (SocketTimeoutException ex) {
			System.out.println("Timeout error: " + ex.getMessage());
			ex.printStackTrace();
		} catch (IOException ex) {
			System.out.println("Client error: " + ex.getMessage());
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}
}
