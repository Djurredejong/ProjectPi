package helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Converter {

	/**
	 * convert file to array of bytes, which can then be send over UDP
	 */
	public byte[] fileToBytes(File file, int mtu) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		int len = (int) file.length();
		byte[] byteArray = new byte[len];

		int off = 0;
//		int numRead = 0;
		while (off < len) {// ((off < len) && (numRead == fis.read(byteArray, off, (len - off)))) {

			try {
				fis.read(byteArray, off, Math.min(mtu, (len - off)));
				System.out.println("reading bytes " + off + " to " + (off + Math.min(mtu, (len - off))));
			} catch (IOException e) {
				e.printStackTrace();
			}

			off += mtu;
		}
		try {
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return byteArray;
	}

	/**
	 * convert array of bytes back to file (upon receiving them via UDP)
	 */
	public void bytesToFile(byte[] byteArray) {
		FileOutputStream fos = null;
		File file = new File("path");
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
