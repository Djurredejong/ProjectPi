package helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Converter {

	/**
	 * Reads the entire file at once
	 */
	public static byte[] fileToBytes(File file) {
		return fileToBytes(file, (int) file.length(), false, false);
	}

	/**
	 * Convert a file to an array of bytes in which each block of "header" + mtu
	 * bytes (seq nr, checksum value, mtu bytes of data) represents one packet to be
	 * send
	 */
	public static byte[] fileToPacketByteArray(File file) {
		return fileToBytes(file, Transfer.getMTU(), true, true);
	}

	/**
	 * Convert file to array of bytes, which can then be send over UDP
	 * 
	 * @param file     The file to be converted
	 * @param readSize The max. number of bytes to read from the file at once
	 * @param seqNr    Set to true to insert a sequence number before every checksum
	 *                 byte, which is followed by a block of "readSize" bytes
	 * @param checksum Set to true to insert a checksum value after every sequence
	 *                 number byte, before every block of "readSize" bytes
	 * @return The array of bytes representing the file
	 */
	public static byte[] fileToBytes(File file, int readSize, boolean withSeqNr, boolean withChecksum) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		int len = (int) file.length();
		if (withSeqNr || withChecksum) {
			len += Transfer.getHeadSize() * (len / readSize + 1);
		}
		byte[] byteArray = new byte[len];

		int off = 0;
		int seqNr = 0;
		int checksum = 0;

		int maxSeqNr = (int) (Math.pow(2, 8) / 2);

		while (off < len) {

			if (withSeqNr || withChecksum) {
				byteArray[off] = (byte) seqNr;
				byteArray[off + 1] = 0;
//				byteArray[off + 2] = (byte) ((checksum >>> 8) & 0xFF);
//				byteArray[off + 3] = (byte) (checksum & 0xFF);
				off += Transfer.getHeadSize();
				seqNr++;
				if (seqNr == maxSeqNr) {
					seqNr = 0;
				}
			}

			try {
				fis.read(byteArray, off, Math.min(readSize, (len - off)));
				if (withChecksum) {
					// Calculate the checksum for the part that has just been read
					// and update the last two checksum bytes
					byte[] dataArray = new byte[Math.min(readSize, (len - off))];
					System.arraycopy(byteArray, off, dataArray, 0, Math.min(readSize, (len - off)));
					checksum = calcChecksum(dataArray);
					byteArray[off - 2] = (byte) ((checksum >>> 8) & 0xFF);
					byteArray[off - 1] = (byte) (checksum & 0xFF);
				} else {
					byteArray[off - 2] = (byte) 0;
					byteArray[off - 1] = (byte) 0;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			off += readSize;

		}
		try {
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return byteArray;
	}

	/**
	 * Calculates the checksum for a packet with data
	 */
	public static int calcChecksum(byte[] dataArray) {
		int checksum = 0;
		byte[] tempArray = new byte[2];
		for (int i = 0; i < dataArray.length - 1; i += 2) {
			tempArray[0] = dataArray[i];
			tempArray[1] = dataArray[i + 1];
			checksum += ((tempArray[1] & 0xFF) << 8) | ((tempArray[0] & 0xFF) << 0);
		}
		checksum = checksum & 32767;
		return checksum;
	}

	/**
	 * Convert array of bytes to file and return that file
	 * 
	 * @param byteArray Array of bytes representing a file
	 * @param pathName  Destination path incl. filename (of choice) + correct
	 *                  extension!
	 * @return The file that was represented by the array of bytes
	 */
	public static File bytesToFile(byte[] byteArray, String pathName) {
		FileOutputStream fos = null;
		File file = new File(pathName);
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			fos.write(byteArray);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	/**
	 * Convert array of bytes to file and return nothing
	 * 
	 * @param byteArray Array of bytes representing a file
	 * @param pathName  Destination path incl. filename (of choice) + correct
	 *                  extension!
	 */
	public static void byteArrayToFile(byte[] byteArray, String pathName) {
		FileOutputStream fos = null;
		File file = new File(pathName);
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			fos.write(byteArray);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Transforms an amount of nanoseconds into a string with hours, minutes and
	 * seconds
	 */
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
	 * Six bytes to int helper function
	 */
	public static int sixBytesToInt(byte[] byteArray) {
		return ((byteArray[5] & 0xFF) << 40) | ((byteArray[4] & 0xFF) << 32) | ((byteArray[3] & 0xFF) << 24)
				| ((byteArray[2] & 0xFF) << 16) | ((byteArray[1] & 0xFF) << 8) | ((byteArray[0] & 0xFF) << 0);
	}

}
