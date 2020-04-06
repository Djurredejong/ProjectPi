package helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Converter {

	/**
	 * Reads the enitre file at once
	 */
	public static byte[] fileToBytes(File file) {
		return fileToBytes(file, (int) file.length());
	}

	/**
	 * Convert file to array of bytes, which can then be send over UDP
	 * 
	 * @param file     The file to be converted
	 * @param readSize The max. number of bytes to read from the file at once
	 * @return The array of bytes representing the file
	 */
	public static byte[] fileToBytes(File file, int readSize) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		int len = (int) file.length();
		byte[] byteArray = new byte[len];

		int off = 0;
		while (off < len) {

			try {
				fis.read(byteArray, off, Math.min(readSize, (len - off)));
				System.out.println("reading bytes " + off + " to " + (off + Math.min(readSize, (len - off))));
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
	 * Convert array of bytes back to file (upon receiving them via UDP)
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

}
