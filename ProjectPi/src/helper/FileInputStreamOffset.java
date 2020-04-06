package helper;

import java.io.FileInputStream;
import java.io.IOException;

public class FileInputStreamOffset {

	public static final String srcPath = "/Users/joris.vandermeulen/git/ProjectPi/ProjectPi/";

	public static void main(String[] args) {

		try {
			FileInputStream fileInputStream = new FileInputStream(srcPath + "Hello.txt");

			byte[] buffer = new byte[4];
			int i = fileInputStream.read(buffer, 0, 1);
			int j = fileInputStream.read(buffer, 2, 1);

			System.out.println("Number of bytes read:- " + (i + j));

			for (byte b : buffer) {
				char c = (char) b;
				if (b == 0)
					c = '-';
				System.out.println("Char read from buffer :- " + c);
			}

			fileInputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
