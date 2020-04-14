package helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class Transfer {

	private static final int headSize = 4;
	private static final int mtu = 512;
	private static final int pktSize = headSize + mtu;

	/**
	 * receiveFile should be called by another program before calling sendFile
	 */
	public static void sendFile(File file, InetAddress address, int port, DatagramSocket socket, double pktLossProb)
			throws IOException {

		// First, send file info (size)
		byte[] bytesSize = new byte[6];
		for (int i = 0; i < 6; i++) {
			bytesSize[i] = (byte) (file.length() >>> (i * 8));
		}
		DatagramPacket sizePkt = new DatagramPacket(bytesSize, 6, address, port);
		// Do not let the size packet get lost:
		sendPacket(sizePkt, socket, 0);

		byte[] bytesFile = Converter.fileToPacketByteArray(file);

		int off = 0;

		while (off < bytesFile.length) {

			DatagramPacket pkt = new DatagramPacket(bytesFile, off, Math.min(pktSize, (bytesFile.length - off)),
					address, port);
			sendPacket(pkt, socket, pktLossProb);

//			if (off > (bytesFile.length - pktSize)) {
//				// Prevent infinite loop by putting a timer on the recursive function.
//			}
			System.out.println("lostPkts =  " + receiveAck(pkt, socket, bytesFile[off], pktLossProb, 0));

			off += pktSize;
		}
	}

	/**
	 * Receive correct acknowledgement or retransmit the packet after timeout
	 */
	public static int receiveAck(DatagramPacket pkt, DatagramSocket socket, byte expSeqNr, double pktLossProb,
			int lostPkts) throws IOException {
		boolean recAck = false;
		byte recSeqNr = 0;
		DatagramPacket ack = new DatagramPacket(new byte[1], 1);
		try {
			socket.setSoTimeout(100);
			socket.receive(ack);
			recSeqNr = ack.getData()[0];
			recAck = true;
		} catch (SocketTimeoutException e) {
			recAck = false;
		}
		if (recAck && (recSeqNr == expSeqNr)) {
			// All good!
			return lostPkts;
		} else {
			// Keep retransmitting and waiting for the correct ack until the transfer
			// succeeds
			sendPacket(pkt, socket, pktLossProb);
			return receiveAck(pkt, socket, expSeqNr, pktLossProb, lostPkts + 1);
		}
	}

	/**
	 * receiveFile should be called before another program calls sendFile
	 */
	public static void receiveFile(String pathName, DatagramSocket socket, double pktLossProb) throws IOException {

		// First, receive file info (size)
		DatagramPacket sizePkt = new DatagramPacket(new byte[6], 6);
		socket.setSoTimeout(0);
		socket.receive(sizePkt);
		byte[] bytesSize = new byte[6];
		for (int i = 0; i < sizePkt.getLength(); i++) {
			bytesSize[i] = sizePkt.getData()[i];
		}
		int fileLength = Converter.sixBytesToInt(bytesSize);

		// Next, receive the packets that make up the file
		byte[] recFileBytes = new byte[fileLength];
		byte[] buf = new byte[pktSize];

		int off = 0;
		int prevSeqNr = -1;
		int seqNr = -1;
		int maxSeqNr = (int) (Math.pow(2, 7));

		while (off < fileLength) {
			prevSeqNr = seqNr;

			DatagramPacket pkt = new DatagramPacket(buf, pktSize);
			socket.setSoTimeout(0);
			socket.receive(pkt);
			seqNr = pkt.getData()[0];

			if (seqNr == prevSeqNr) {
				// do not copy the data of the received packet into recFileBytes
			} else if (seqNr == prevSeqNr + 1 || (prevSeqNr == (maxSeqNr - 1) && seqNr == 0)) {

				int expChecksum = ((pkt.getData()[2] & 0xFF) << 8) | ((pkt.getData()[3] & 0xFF) << 0);
				byte[] dataArray = new byte[Math.min(mtu, (fileLength - off))];
				System.arraycopy(pkt.getData(), headSize, dataArray, 0, Math.min(mtu, (fileLength - off)));
				int checksum = Converter.calcChecksum(dataArray);

				if (expChecksum != checksum) {
					// Checksum test failed, do not copy the data and request same package
					seqNr = prevSeqNr;
				} else {
					System.arraycopy(dataArray, 0, recFileBytes, off, Math.min(mtu, (fileLength - off)));
					off += mtu;
				}
			} else {
				// Should actually not be allowed to happen, do not copy the data
			}

			// Send seqNr back as acknowledgement
			DatagramPacket ack = new DatagramPacket(new byte[] { pkt.getData()[0] }, 1, pkt.getAddress(),
					pkt.getPort());

			if (off > (fileLength - pktSize)) {
				// This was the very last packet, do not let the ack get lost:
				sendPacket(ack, socket, 0);
			} else {
				sendPacket(ack, socket, pktLossProb);
			}
		}
		// Finally, convert all the received data from the packets into the file
		Converter.byteArrayToFile(recFileBytes, pathName);
	}

	/**
	 * Similar to the method this one overloads, but allows for user input. Call
	 * this method when downloading a file, so that the download can be
	 * paused/resumed and statistics can be requested by the user.
	 */
	public static void receiveFile(String pathName, DatagramSocket socket, double pktLossProb, BufferedReader in)
			throws IOException {

		// First, receive file info (size)
		DatagramPacket sizePkt = new DatagramPacket(new byte[6], 6);
		socket.setSoTimeout(0);
		socket.receive(sizePkt);
		byte[] bytesSize = new byte[6];
		for (int i = 0; i < sizePkt.getLength(); i++) {
			bytesSize[i] = sizePkt.getData()[i];
		}
		int fileLength = Converter.sixBytesToInt(bytesSize);

		// Next, receive the packets that make up the file
		byte[] recFileBytes = new byte[fileLength];
		byte[] buf = new byte[pktSize];

		int off = 0;
		int prevSeqNr = -1;
		int seqNr = -1;
		int maxSeqNr = (int) (Math.pow(2, 7));

		long startTime = System.nanoTime();
		long pauseTotalTime = 0;
		System.out.println();
		System.out.println("Downloading has begun. At any time during this process, you can");
		System.out.println("type <p> to pause downloading the file,");
		System.out.println(" or  <s> to show some statistics regarding the download.");
		System.out.println("Press the Return key after typing one of these command characters.");
		int recPackets = 0;
		int packetsToRec = fileLength / mtu;

		while (off < fileLength) {
			prevSeqNr = seqNr;

			if (seqNr == (maxSeqNr - 1)) {
				pauseTotalTime = checkInput(in, recPackets, packetsToRec, startTime, pauseTotalTime);
			}

			DatagramPacket pkt = new DatagramPacket(buf, pktSize);
			socket.setSoTimeout(0);
			socket.receive(pkt);
			seqNr = pkt.getData()[0];

			if (seqNr == prevSeqNr) {
				// do not copy the data of the received packet into recFileBytes
			} else if (seqNr == prevSeqNr + 1 || (prevSeqNr == (maxSeqNr - 1) && seqNr == 0)) {

				int expChecksum = ((pkt.getData()[2] & 0xFF) << 8) | ((pkt.getData()[3] & 0xFF) << 0);
				byte[] dataArray = new byte[Math.min(mtu, (fileLength - off))];
				System.arraycopy(pkt.getData(), headSize, dataArray, 0, Math.min(mtu, (fileLength - off)));
				int checksum = Converter.calcChecksum(dataArray);

				if (expChecksum != checksum) {
					// Checksum test failed, do not copy the data and request same package
					seqNr = prevSeqNr;
				} else {
					System.arraycopy(dataArray, 0, recFileBytes, off, Math.min(mtu, (fileLength - off)));
					recPackets++;
					off += mtu;
				}
			} else {
				// Should actually not be allowed to happen, do not copy the data
			}

			// Send seqNr back as acknowledgement
			DatagramPacket ack = new DatagramPacket(new byte[] { pkt.getData()[0] }, 1, pkt.getAddress(),
					pkt.getPort());

			if (off > (fileLength - pktSize)) {
				// This was the very last packet, do not let the ack get lost:
				sendPacket(ack, socket, 0);
			} else {
				sendPacket(ack, socket, pktLossProb);
			}
		}

		// Show some statistics after finishing the download
		long timeTaken = System.nanoTime() - startTime - pauseTotalTime;
		System.out.println();
		System.out.println("The file has been downloaded!");
		System.out.println("It has taken " + Converter.nanoToTime(timeTaken));
		System.out.println("The download speed was "
				+ String.format("%.2f", (8000 * (double) fileLength / (double) timeTaken)) + " Mbps");
		System.out.println("UNKNOWN" + " times a packet had to be retransmitted.");

		// Finally, convert all the received data from the packets into the file
		Converter.byteArrayToFile(recFileBytes, pathName);
	}

	/**
	 * Transmits a DatagramPacket via the provided socket, or not (simulating packet
	 * loss)
	 */
	public static void sendPacket(DatagramPacket pkt, DatagramSocket socket, double pktLossProb) throws IOException {
		if (pktLossProb == 0) {
			socket.send(pkt);
		} else {
			if (Math.random() > pktLossProb) {
				socket.send(pkt);
			} else {
				// The packet got lost!
			}
		}
	}

	/**
	 * Call this method every X packets to check for user input when downloading a
	 * file
	 */
	public static long checkInput(BufferedReader in, int recPackets, int packetsToRec, long startTime,
			long pauseTotalTime) throws IOException {
		if (in.ready()) {
			String input = in.readLine();
			String[] split = input.split("\\s");
			String cmd = split[0];
			if (split.length > 1) {
				System.out.println("Please provide only one command character before pressing the return key.");
			}
			if (cmd.length() == 1) {
				char command = cmd.charAt(0);
				switch (command) {
				case ('p'):
					long pauseStartTime = System.nanoTime();
					System.out.println("Downloading the file has been paused.");
					System.out.println("It will continue as soon as you press the return key.");
					System.out.println("Until then, none of your input will be registered.");
					in.readLine();
					System.out.println("Downloading the file will continue.");
					long pauseEndTime = System.nanoTime();
					pauseTotalTime += (pauseEndTime - pauseStartTime);
					break;
				case ('s'):
					System.out.println("Some statistics regarding the download:");
					long timePassed = System.nanoTime() - startTime - pauseTotalTime;
					System.out.println("The total transmission time is " + Converter.nanoToTime(timePassed));
					System.out.println(recPackets + " out of " + packetsToRec + " have been succesfully received.");
					System.out.println("UNKNOWN" + " times a packet had to be retransmitted.");
					break;
				default:
					System.out.println("The character you have typed is not one of the valid command characters.");
					break;
				}
			} else {
				System.out.println("Please provide only one command character before pressing the return key.");
			}
		}
		return pauseTotalTime;
	}

	/**
	 * Converter needs to know this when converting file to packets
	 */
	public static int getMTU() {
		return mtu;
	}

	/**
	 * Converter needs to know this when converting file to packets
	 */
	public static int getHeadSize() {
		return headSize;
	}

}
