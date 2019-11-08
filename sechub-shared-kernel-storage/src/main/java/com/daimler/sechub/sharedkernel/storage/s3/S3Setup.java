// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.storage.s3;

public interface S3Setup {

	String getAccessKey();

	String getSecretkey();

	String getEndPoint();

	String getBucketName();

	boolean isAvailable();

}