// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.storage.filesystem;

public interface SharedVolumeSetup {

	String getUploadDir();

	boolean isAvailable();

}