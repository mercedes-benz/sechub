// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextFileWriter {

	private static final Logger LOG = LoggerFactory.getLogger(TextFileWriter.class);

	private LicenseHeaderProvider licenseHeaderProvider;

	public TextFileWriter() {
		licenseHeaderProvider = new LicenseHeaderProvider();
	}

	public void addMissingHeaders(File targetFile) throws IOException {
		/* read content */
		String origin = read(targetFile);
		if (origin.contains(LicenseHeaderProvider.LICENSE_SPDX_IDENTIFIER)) {
			/* already contained - ignore*/
			return;
		}
		/* do save, will add header if necessary */
		save(targetFile, origin, true);
	}

	private String read(File file) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = Files.newBufferedReader(file.toPath())) {
			String readLine = null;
			boolean notFirstLine=false;
			while ((readLine = br.readLine())!=null) {
				if (notFirstLine) {
					sb.append("\n");
				}
				notFirstLine=true;
				sb.append(readLine);
			}
		}
		return sb.toString();
	}

	/**
	 * Save text file, does overwrite existing ones! Adds missing headers
	 *
	 * @param targetFile
	 * @param origin
	 * @throws IOException
	 */
	public void save(File targetFile, String origin) throws IOException {
		save(targetFile, origin, true);
	}

	/**
	 * Save text file, does overwrite existing ones (when wanted only! Adds missing
	 * headers when writing and header is necessary
	 *
	 * @param targetFile
	 * @param origin
	 * @param overwrite
	 * @throws IOException
	 */
	public void save(File targetFile, String origin, boolean overwrite) throws IOException {
		if (targetFile == null) {
			throw new IllegalArgumentException("null not allowed as file!");
		}
		String licenseHeader = licenseHeaderProvider.getLicenseHeader(origin, targetFile);
		String text;
		if (licenseHeader == null) {
			text = origin;
		} else {
			text = licenseHeader + "\n" + origin;
		}

		if (targetFile.exists()) {
			if (!overwrite) {
				LOG.warn("Already existing and 'overwrite' not enabled:" + targetFile);
				return;
			}
			/*
			 * Use old API and not Files.delete(..) - reason: I want not to accidently
			 * delete a folder! With old API it is ensured this is only a file not a dir
			 */
			if (/* NOSONAR */!targetFile.delete()) {
				throw new IOException("was not able to delete existing file:" + targetFile);
			}
		}

		if (!targetFile.exists()) {
			File parentFile = targetFile.getParentFile();
			if (!parentFile.exists() && !parentFile.mkdirs()) {
				throw new IllegalStateException("Not able to create folder structure for:" + targetFile);
			}
			if (!targetFile.createNewFile()) {
				throw new IllegalStateException("was not able to create new file:" + targetFile);
			}
		}
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(targetFile))) {
			bw.write(text);
		}
		LOG.info("Written:" + targetFile);
	}
}
