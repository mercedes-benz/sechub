// SPDX-License-Identifier: MIT
package com.daimler.sechub.storage.core;

public interface S3Setup extends StorageSetup {

	String getAccessKey();

	String getSecretkey();

	String getEndPoint();

	String getBucketName();

}