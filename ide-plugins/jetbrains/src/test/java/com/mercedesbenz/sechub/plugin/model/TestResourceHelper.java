// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.model;

import java.io.File;
import java.nio.file.Path;

public class TestResourceHelper {
	private static File srcTestResources = new File("src/test/resources");
	static {
		if (!srcTestResources.exists()) {
			srcTestResources = new File("sechub-eclipse-plugin/src/test/resources");
		}
		if (!srcTestResources.exists()) {
			throw new IllegalStateException("Cannot find test resource directory inside plugin project!");
		}
	}

	public static Path getEnsuredTestPath(String path) {
		return getEnsuredTestFile(path).toPath();
	}
	
	public static File getEnsuredTestFile(String path) {
		File file = new File(srcTestResources,path);
		if (! file.exists()) {
			throw new IllegalStateException("Cannot determine test file:"+file.getAbsolutePath());
		}
		return file;
	}

}
