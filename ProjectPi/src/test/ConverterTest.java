package test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import helper.Converter;

class ConverterTest {

	public static final String srcPath = "/Users/joris.vandermeulen/git/ProjectPi/ProjectPi/";

	@BeforeEach
	void setUp() throws Exception {
//		converter = new Converter();
	}

	@Test
	void testFileToBytes() {
		File file = new File(srcPath + "Tiny.pdf");
		byte data[] = Converter.fileToBytes(file, 512, false, false);
		assertEquals(24286, data.length);
		assertEquals(67, data[1330]);
	}

	@Test
	void testBytesToFile() {
		File file = new File(srcPath + "Tiny.pdf");
		byte data[] = Converter.fileToBytes(file, 512, false, false);
		String path = srcPath + "/test/Tiny.pdf";
		File outputFile = Converter.bytesToFile(data, path);
		byte outputData[] = Converter.fileToBytes(outputFile, 2345, false, false);
		assertEquals(data.length, outputData.length);
		for (int i = 0; (i < outputData.length && i < data.length); i++) {
			assertEquals(data[i], outputData[i]);
		}
		outputFile.delete();
	}

	@Test
	void testFileToPacketByteArray() {
		File file = new File(srcPath + "Tiny.pdf");
		byte data[] = Converter.fileToBytes(file, 512, true, true);
		assertEquals(24286 + 4 * (24286 / 512 + 1), data.length);
		assertEquals(67, data[1342]);
	}
}
