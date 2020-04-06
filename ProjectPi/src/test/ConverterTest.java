package test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import helper.Converter;

class ConverterTest {

	public static final String srcPath = "/Users/joris.vandermeulen/git/ProjectPi/ProjectPi/";

	public Converter converter;

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
		File file = new File(srcPath + "Empty.pdf");
		byte data[] = converter.fileToBytes(file, 512);
		File newFile = new File(srcPath);
		converter.bytesToFile(data);
//		assertEquals(newFile, srcPath + "Empty.pdf");
	}
}
