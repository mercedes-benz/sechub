// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model;

import java.util.ArrayList;
import java.util.List;

public class SecHubFileSystemConfiguration {

	public static final String PROPERTY_FOLDERS = "folders";

	private List<String> folders = new ArrayList<>();

	public List<String> getFolders() {
		return folders;
	}

}
