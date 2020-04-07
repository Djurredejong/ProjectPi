package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class Client {

	private DatagramSocket socket;
	/** according to my protocol: 2B seqNr, 2B checksum, 512B data */
	private static final int pktSize = 516;

	public static void main(String[] args) {

		String hostname = "localhost";
		int port = 9999;

		try {
			
			Client client = 
			
			InetAddress address = InetAddress.getByName(hostname);
			DatagramSocket socket = new DatagramSocket();

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

	private void receiveFile() throws IOException {
		// TODO recFileLength to be received as first response to request
		int recFileLength = (3961 + 4 * (3961 % 512 + 1));
		byte[] buf = new byte[pktSize];

		int off = 0;
		while (off < recFileLength) {

			DatagramPacket pkt = new DatagramPacket(buf, pktSize);
			socket.receive(pkt);

			off += pktSize;
		}
	}
}
