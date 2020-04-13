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

		int maxSeqNr = (int) (Math.pow(2, (8 * 2)) / 2);

		while (off < len) {

			if (withSeqNr || withChecksum) {
				byteArray[off] = (byte) ((seqNr >>> 8) & 0xFF);
				byteArray[off + 1] = (byte) (seqNr & 0xFF);
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
					byteArray[off - 2] = 0;
					byteArray[off - 1] = 0;
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

	private static int calcChecksum(byte[] dataArray) {
		return 0;
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

}
