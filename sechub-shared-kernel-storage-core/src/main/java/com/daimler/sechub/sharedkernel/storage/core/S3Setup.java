// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.storage.core;

public interface S3Setup {

	String getAccessKey();

	String getSecretkey();

	String getEndPoint();

	String getBucketName();

	boolean isAvailable();

}