// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipFile;

import org.springframework.stereotype.Component;

@Component
public class PDSZipSupport {

	public boolean isZipFile(Path pathToFile) {
		try (ZipFile zipFile = new ZipFile(pathToFile.toFile())){
			return true;
		} catch (IOException e) {
			return false;
		}
	}

}
