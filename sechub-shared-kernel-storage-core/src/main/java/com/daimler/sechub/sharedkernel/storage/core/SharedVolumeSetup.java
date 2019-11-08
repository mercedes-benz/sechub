// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.storage.core;

public interface SharedVolumeSetup {

	String getUploadDir();

	boolean isAvailable();

}