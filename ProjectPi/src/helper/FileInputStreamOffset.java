package helper;

import java.io.FileInputStream;
import java.io.IOException;

public class FileInputStreamOffset {

	public static final String srcPath = "/Users/joris.vandermeulen/git/ProjectPi/ProjectPi/";

	public static void main(String[] args) {

		try {
			FileInputStream fileInputStream = new FileInputStream(srcPath + "Hello.txt");

			byte[] buffer = new byte[8];
			int k = 0;
			while (k < 8) {
				fileInputStream.read(buffer, k, 2);
				for (byte b : buffer) {
					char c = (char) b;
					if (b == 0) {
						c = '-';
					}
					System.out.println("Char read from buffer :- " + c);
				}
				k += 2;
			}

			fileInputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
