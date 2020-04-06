package test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import helper.Converter;

class ConverterTest {

	public static final String srcPath = "/Users/joris.vandermeulen/git/ProjectPi/ProjectPi/";

	private Converter converter;

	@BeforeEach
	void setUp() throws Exception {
		converter = new Converter();
	}

	@Test
	void testFileToBytes() {
		File file = new File(srcPath + "Empty.pdf");
		byte data[] = converter.fileToBytes(file, 512);
		assertEquals(3961, data.length);
		assertEquals(66, data[1330]);
	}

	@Test
	void testBytesToFile() {
		File file = new File(srcPath + "Tiny.pdf");
		byte data[] = converter.fileToBytes(file, 512);
		String path = srcPath + "/temp/Tiny.pdf";
		File outputFile = converter.bytesToFile(data, path);
		byte outputData[] = converter.fileToBytes(outputFile, 2345);
		assertEquals(data.length, outputData.length);
		for (int i = 0; (i < outputData.length && i < data.length); i++) {
			assertEquals(data[i], outputData[i]);
		}
	}
}
