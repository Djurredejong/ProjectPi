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
//		testListFiles();
		testNanoToTime();
	}

	private static void testNanoToTime() {
		long nanos = 100000000000000L;
		long totalSec = nanos / (1000 * 1000 * 1000);
		long sec = totalSec % 60;
		long min = (totalSec / 60) % 60;
		long hour = (totalSec / (60 * 60)) % 24;
		System.out.println(hour + " hours, " + min + " minutes, and " + sec + " seconds");
	}

	private static void testListFiles() {
		String current = System.getProperty("user.dir");
		System.out.println(current);

		File file = new File(current);
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
