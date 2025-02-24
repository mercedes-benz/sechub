// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DragAndDropData {

	private List<File> files = new ArrayList<>();

	public void add(File file) {
		this.files.add(file);
	}
	
	public List<File> getFiles() {
		return files;
	}

	public File getFirstFileOrNull() {
		if (files.isEmpty()){
			return null;
		}
		return files.get(0);
	}

}
