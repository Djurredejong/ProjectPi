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
	public byte[] fileToBytes(File file) {
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
				fis.read(byteArray, off, (len - off));
			} catch (IOException e) {
				e.printStackTrace();
			}
			off += len;
		}
		try {
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return byteArray;

		// next part requires java.nio

//		Path fPath = Paths.get(srcPath + "Tiny.pdf");
//		byte[] byteArray = null;
//		try {
//			byteArry = Files.readAllBytes(fPath);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
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