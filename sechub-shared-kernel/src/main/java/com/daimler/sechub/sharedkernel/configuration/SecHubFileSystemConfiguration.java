// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.configuration;

import java.util.ArrayList;
import java.util.List;

public class SecHubFileSystemConfiguration {

	public static final String PROPERTY_FOLDERS = "folders";

	private List<String> folders = new ArrayList<>();

	public List<String> getFolders() {
		return folders;
	}

}
