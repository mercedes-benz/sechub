// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.core;

public interface S3Setup extends StorageSetup {

    String getAccessKey();

    String getSecretkey();

    String getEndPoint();

    String getBucketName();

}