// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model;

import java.util.Optional;

public class SecHubCodeScanConfiguration {

	public static final String PROPERTY_FILESYSTEM="fileSystem";
	
	private Optional<SecHubFileSystemConfiguration> fileSystem= Optional.empty();

	public void setFileSystem(SecHubFileSystemConfiguration fileSystem) {
		this.fileSystem = Optional.ofNullable(fileSystem);
	}
	
	public Optional<SecHubFileSystemConfiguration> getFileSystem() {
		return fileSystem;
	}

}
