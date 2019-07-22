// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipFile;

public class ZipSupport {
	
	/**
	 * A shared instance
	 */
	public static final ZipSupport INSTANCE = new ZipSupport();

	public boolean isZipFile(Path pathToFile) {
		try (ZipFile zipFile = new ZipFile(pathToFile.toFile())){
			return true;
		} catch (IOException e) {
			return false;
		}
	}

}
