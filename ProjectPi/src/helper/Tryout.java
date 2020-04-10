package helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Tryout {
	public static final String srcPath = "/Users/joris.vandermeulen/git/ProjectPi/ProjectPi/";

	public static void main(String[] args) {
//		testFileInputStream();
//		testBytesToStrings();
		testListFiles();
	}

	private static void testListFiles() {
		File file = new File(srcPath);
		File[] files = file.listFiles();
		for (File f : files) {
			System.out.println(f.getName());
		}
	}

	private static void testBytesToStrings() {
		String str = "d /File";
		byte[] b = str.getBytes(StandardCharsets.UTF_8);
		for (int i = 0; i < b.length; i++) {
			System.out.println(b[i]);
		}

		byte[] bytes = { (byte) 100, (byte) 32, (byte) 47, (byte) 70 };
		String s = new String(bytes, StandardCharsets.UTF_8);
		System.out.println(s);
	}

	private static void testFileInputStream() {
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
