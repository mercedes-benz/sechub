// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.util;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.junit.Before;
import org.junit.Test;

public class TextFileWriterTest {
	
	private TextFileWriter writerToTest;


	@Before
	public void before() throws Exception {
		writerToTest = new TextFileWriter();
	}


	@Test
	public void is_able_to_save_a_temporary_file() throws Exception {
		/* prepare */
		File file = new File(System.getProperty("java.io.tmpdir"));
		File subFolder = new File(file, "subFolder");
		File targetFile = new File(subFolder, "targetFile");
		targetFile.deleteOnExit();

		/* execute */
		writerToTest.save(targetFile, "text");

		/* test */
		assertTrue(targetFile.exists());

		try (BufferedReader br = new BufferedReader(new FileReader(targetFile))) {
			String line = br.readLine();
			assertEquals("text", line);
		}
	}
}
