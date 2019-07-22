// SPDX-License-Identifier: MIT
package com.daimler.sechub.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SechubTestComponent
public class TestUtil {

	private static final OperationSystem operationSystem = new OperationSystem();

	public static void waitMilliseconds(long milliseconds) {
		try {
			Thread.sleep(milliseconds); // NOSONAR
		} catch (InterruptedException e) {
			throw new IllegalStateException("Testcase szenario corrupt / should not happen", e);
		}

	}

	public static boolean isDeletingTempFiles() {
		return !isKeepingTempfiles();
	}

	public static boolean isKeepingTempfiles() {
		if (Boolean.getBoolean(System.getenv("SECHUB_KEEP_TEMPFILES"))){
			return true;
		}
		return false;
	}

	public static boolean isWindows() {
		return operationSystem.isWindows();
	}

	public static void unzip(final File zipFile, final Path unzipTo) throws IOException{
		try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile.toPath()))) {
			ZipEntry entry;
			while ((entry = zipInputStream.getNextEntry()) != null) {
				final Path toPath = unzipTo.resolve(entry.getName());
				if (entry.isDirectory()) {
					Files.createDirectory(toPath);
				} else {
					// just ensure parent files are really available...
					toPath.toFile().getParentFile().mkdirs();
					Files.copy(zipInputStream, toPath);
				}
			}
		}
	}

	private static class OperationSystem{

		private boolean windows;

		OperationSystem(){
			String os = System.getProperty("os.name").toLowerCase();;
			windows = (os.indexOf("win") >= 0);
		}

		public boolean isWindows() {
			return windows;
		}
	}

}
