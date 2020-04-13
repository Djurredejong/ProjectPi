package helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class Transfer {
	/**
	 * The "header" consists of a 2B seqNr and an unused 2B (perhaps to be used as
	 * checksum for data integrity)
	 */
	private static final int headSize = 4;
	private static final int mtu = 512;
	private static final int pktSize = headSize + mtu;

	/**
	 * receiveFile should be called by another program before calling sendFile
	 * 
	 * @param file        The file to be send
	 * @param address     The address to send to
	 * @param port        The port to send on
	 * @param socket      The socket to send via
	 * @param pktLossProb Probability that a packet being send by the sender of the
	 *                    file gets lost. Set to 0 if you do not want to simulate
	 *                    packet loss on the packets being send by the sender of the
	 *                    file
	 */
	public static void sendFile(File file, InetAddress address, int port, DatagramSocket socket, double pktLossProb)
			throws IOException {

		// First, send file info (size)
		byte[] bytesSize = new byte[6];
		for (int i = 0; i < 6; i++) {
			bytesSize[i] = (byte) (file.length() >>> (i * 8));
		}
		DatagramPacket sizePkt = new DatagramPacket(bytesSize, 4, address, port);
		// Do not let the size packet get lost:
		sendPacket(sizePkt, socket, 0);

		byte[] bytesFile = Converter.fileToPacketByteArray(file);

		int off = 0;
		byte[] bytesSeq = new byte[2];

		while (off < bytesFile.length) {

			DatagramPacket pkt = new DatagramPacket(bytesFile, off, Math.min(pktSize, (bytesFile.length - off)),
					address, port);
			sendPacket(pkt, socket, pktLossProb);

			bytesSeq[0] = bytesFile[off];
			bytesSeq[1] = bytesFile[off + 1];
//			if (off > (bytesFile.length - pktSize)) {
//				// TODO When sending the very last packet and the ack for it gets lost, prevent
//				// getting stuck in an infinite loop -> Timer on the recursive function.
//			}
			receiveAck(pkt, socket, bytesSeq, pktLossProb);

			off += pktSize;
		}

	}

	/**
	 * Receive correct acknowledgement or retransmit the packet after timeout
	 */
	public static void receiveAck(DatagramPacket pkt, DatagramSocket socket, byte[] bytesSeq, double pktLossProb)
			throws IOException {
		boolean recAck = false;
		byte[] bytesAck = new byte[2];
		DatagramPacket ack = new DatagramPacket(new byte[2], 2);
		try {
			socket.setSoTimeout(100);
			socket.receive(ack);
			bytesAck[0] = ack.getData()[0];
			bytesAck[1] = ack.getData()[1];
//			System.out.println("Received ack with sequence number " + twoBytesToInt(bytesAck));
			recAck = true;
		} catch (SocketTimeoutException e) {
			recAck = false;
		}
		if (recAck && Arrays.equals(bytesAck, bytesSeq)) {
			// All good!
		} else {
			// Keep retransmitting and waiting for the correct ack until the transfer
			// succeeds
			sendPacket(pkt, socket, pktLossProb);
			receiveAck(pkt, socket, bytesSeq, pktLossProb);
		}
	}

	/**
	 * receiveFile should be called before another program calls sendFile
	 * 
	 * @param pathName    The path (incl. filename and extension) where the file
	 *                    should be stored
	 * @param fileLength  The lenght of the file to be received
	 * @param socket      The socket the file is being send via
	 * @param pktLossProb Probability that a packet (ack) being send by the receiver
	 *                    of the file gets lost. Set to 0 if you do not want to
	 *                    simulate packet loss on the packets (acks) being send by
	 *                    the receiver of the file
	 */
	public static void receiveFile(String pathName, DatagramSocket socket, double pktLossProb) throws IOException {

		// First, receive file info (size)
		DatagramPacket sizePkt = new DatagramPacket(new byte[4], 4);
		socket.setSoTimeout(0);
		socket.receive(sizePkt);
		byte[] bytesSize = new byte[6];
		for (int i = 0; i < sizePkt.getLength(); i++) {
			bytesSize[i] = sizePkt.getData()[i];
		}
		int fileLength = sixBytesToInt(bytesSize);
//		System.out.println("the filelength is " + fileLength);

		// Next, receive the packets that make up the file
		byte[] recFileBytes = new byte[fileLength];
		byte[] buf = new byte[pktSize];

		int off = 0;
		int prevSeqNr = -1;
		int seqNr = -1;
		int maxSeqNr = (int) (Math.pow(2, (8 * 2)) / 2);

		while (off < fileLength) {
			prevSeqNr = seqNr;

			DatagramPacket pkt = new DatagramPacket(buf, pktSize);
			socket.setSoTimeout(0);
			socket.receive(pkt);

			seqNr = (pkt.getData()[0] << 8) | (pkt.getData()[1] & 0xFF);
//			System.out.println("Received packet with sequence number " + seqNr + " of length " + pkt.getLength());

			InetAddress address = pkt.getAddress();
			int port = pkt.getPort();
			if (seqNr == prevSeqNr) {
				// In case the packet is the same as the last one we received, we do not copy
				// the data of the received packet into recFileBytes
			} else if (seqNr == prevSeqNr + 1 || (prevSeqNr == (maxSeqNr - 1) && seqNr == 0)) {
				// In case the packet is not the same as the last one we received, we copy the
				// data of the received packet into recFileBytes, provided that we do not need
				// to discard the package due to the checksum telling us there has been a bit
				// error
				byte[] checksumArray = new byte[2];
				checksumArray[0] = pkt.getData()[3];
				checksumArray[1] = pkt.getData()[2];
				int expChecksum = twoBytesToInt(checksumArray);

				byte[] dataArray = new byte[Math.min(mtu, (fileLength - off))];
				System.arraycopy(pkt.getData(), headSize, dataArray, 0, Math.min(mtu, (fileLength - off)));
				int checksum = Converter.calcChecksum(dataArray);

				if (expChecksum == checksum) {
					System.arraycopy(dataArray, 0, recFileBytes, off, Math.min(mtu, (fileLength - off)));
					off += mtu;
				} else {
					// If the checksum tells us that there is a bit error, we discard the package
					// and inform the sender about this by setting the seqNr equal to the prevSeqNr
					seqNr = prevSeqNr;
				}
			} else {
				System.out.println("Error: very unexpected sequence number");
			}

			// Send seqNr back as acknowledgement
			byte[] bytesAck = new byte[2];
			bytesAck[0] = pkt.getData()[0];
			bytesAck[1] = pkt.getData()[1];
			DatagramPacket ack = new DatagramPacket(bytesAck, 2, address, port);

			if (off > (fileLength - pktSize)) {
				// This was the very last packet, do not let the ack get lost:
				sendPacket(ack, socket, 0);
			} else {
				sendPacket(ack, socket, pktLossProb);
			}
//			System.out.println("Sent ack with sequence number " + twoBytesToInt(bytesAck));
//			System.out.println("The offset is " + off);
		}

		// Finally, convert all the received data from the packets into the file
//		System.out.println("The file has been received!");
		Converter.byteArrayToFile(recFileBytes, pathName);
	}

	/**
	 * Similar to the method above, but allows for user input. Call this method when
	 * downloading a file, so that the download can be paused/resumed and statistics
	 * can be requested by the user.
	 */
	public static void receiveFile(String pathName, DatagramSocket socket, double pktLossProb, BufferedReader in)
			throws IOException {

		// First, receive file info (size)
		DatagramPacket sizePkt = new DatagramPacket(new byte[4], 4);
		socket.setSoTimeout(0);
		socket.receive(sizePkt);
		byte[] bytesSize = new byte[6];
		for (int i = 0; i < sizePkt.getLength(); i++) {
			bytesSize[i] = sizePkt.getData()[i];
		}
		int fileLength = sixBytesToInt(bytesSize);
//		System.out.println("the filelength is " + fileLength);

		// Next, receive the packets that make up the file
		byte[] recFileBytes = new byte[fileLength];
		byte[] buf = new byte[pktSize];

		int off = 0;
		int prevSeqNr = -1;
		int seqNr = -1;
		int maxSeqNr = (int) (Math.pow(2, (8 * 2)) / 2);

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

			if (seqNr % 100 == 0) {
				pauseTotalTime = checkInput(in, recPackets, packetsToRec, startTime, pauseTotalTime);
			}

			DatagramPacket pkt = new DatagramPacket(buf, pktSize);
			socket.setSoTimeout(0);
			socket.receive(pkt);

			seqNr = (pkt.getData()[0] << 8) | (pkt.getData()[1] & 0xFF);
//			System.out.println("Received packet with sequence number " + seqNr + " of length " + pkt.getLength());
			recPackets++;

			InetAddress address = pkt.getAddress();
			int port = pkt.getPort();

//			System.out.println("The seqNr is " + seqNr);

			if (seqNr == prevSeqNr) {
				// In case the packet is the same as the last one we received, we do not copy
				// the data of the received packet into recFileBytes
			} else if (seqNr == prevSeqNr + 1 || (prevSeqNr == (maxSeqNr - 1) && seqNr == 0)) {
				// In case the packet is not the same as the last one we received, we copy the
				// data of the received packet into recFileBytes, provided that we do not need
				// to discard the package due to the checksum telling us there has been a bit
				// error
				byte[] checksumArray = new byte[2];
				checksumArray[0] = pkt.getData()[3];
				checksumArray[1] = pkt.getData()[2];
				int expChecksum = twoBytesToInt(checksumArray);

				byte[] dataArray = new byte[Math.min(mtu, (fileLength - off))];
				System.arraycopy(pkt.getData(), headSize, dataArray, 0, Math.min(mtu, (fileLength - off)));
				int checksum = Converter.calcChecksum(dataArray);

				if (expChecksum == checksum) {
					System.arraycopy(dataArray, 0, recFileBytes, off, Math.min(mtu, (fileLength - off)));
					off += mtu;
				} else {
					// If the checksum tells us that there is a bit error, we discard the package
					// and inform the sender about this by setting the seqNr equal to the prevSeqNr
					seqNr = prevSeqNr;
				}
			} else {
				System.out.println("Error: very unexpected sequence number.");
			}

			// Send seqNr back as acknowledgement
			byte[] bytesAck = new byte[2];
			bytesAck[0] = pkt.getData()[0];
			bytesAck[1] = pkt.getData()[1];
			DatagramPacket ack = new DatagramPacket(bytesAck, 2, address, port);

			if (off > (fileLength - pktSize)) {
				// This was the very last packet, do not let the ack get lost:
				sendPacket(ack, socket, 0);
			} else {
				sendPacket(ack, socket, pktLossProb);
			}
//			System.out.println("Sent ack with sequence number " + twoBytesToInt(bytesAck));
//			System.out.println("The offset is " + off);
		}

		// Finally, show some statistics and convert all the received data from the
		// packets into the file
		long timeTaken = System.nanoTime() - startTime - pauseTotalTime;
		System.out.println();
		System.out.println("The file has been downloaded!");
		System.out.println("It has taken " + nanoToTime(timeTaken));
		System.out.println("fileLength = " + fileLength);
		System.out.println("timeTaken = " + timeTaken);
		System.out.println("The download speed was "
				+ String.format("%.2f", (8000 * (double) fileLength / (double) timeTaken)) + " Mbps");
		System.out.println("UNKNOWN" + " times a packet had to be retransmitted.");
		Converter.byteArrayToFile(recFileBytes, pathName);
	}

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
					System.out.println("The total transmission time is " + nanoToTime(timePassed));
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

	public static String nanoToTime(long nanoTime) {
		long totalSec = nanoTime / (1000 * 1000 * 1000);
		long sec = totalSec % 60;
		long min = (totalSec / 60) % 60;
		long hour = (totalSec / (60 * 60)) % 24;
		if (hour == 0 && min == 0) {
			return (sec + ((sec == 1) ? " second." : " seconds."));
		} else if (hour == 0) {
			return (min + ((min == 1) ? " minute" : " minutes") + " and " + sec
					+ ((sec == 1) ? " second." : " seconds."));
		} else {
			return (hour + ((hour == 1) ? " hour, " : " hours, ") + min + ((min == 1) ? " minute, " : " minutes, ")
					+ " and " + sec + ((sec == 1) ? " second." : " seconds."));
		}
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

	public static int twoBytesToInt(byte[] byteArray) {
		return ((byteArray[1] & 0xFF) << 8) | ((byteArray[0] & 0xFF) << 0);
	}

	public static int fourBytesToInt(byte[] byteArray) {
		return ((byteArray[3] & 0xFF) << 24) | ((byteArray[2] & 0xFF) << 16) | ((byteArray[1] & 0xFF) << 8)
				| ((byteArray[0] & 0xFF) << 0);
	}

	public static int sixBytesToInt(byte[] byteArray) {
		return ((byteArray[5] & 0xFF) << 40) | ((byteArray[4] & 0xFF) << 32) | ((byteArray[3] & 0xFF) << 24)
				| ((byteArray[2] & 0xFF) << 16) | ((byteArray[1] & 0xFF) << 8) | ((byteArray[0] & 0xFF) << 0);
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
