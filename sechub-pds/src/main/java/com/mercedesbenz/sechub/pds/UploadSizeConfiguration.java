// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UploadSizeConfiguration {

    private static final long DEFAULT_MAX_UPLOAD_SIZE_IN_BYTES = 50 * 1024 * 1024; // 50 MiB

    @PDSMustBeDocumented("Define the maximum amount of bytes accepted for uploading files. The default when not set is " + DEFAULT_MAX_UPLOAD_SIZE_IN_BYTES
            + " (" + (DEFAULT_MAX_UPLOAD_SIZE_IN_BYTES / 1024 / 1024) + " MiB)")
    @Value("${pds.upload.binaries.maximum.bytes:" + DEFAULT_MAX_UPLOAD_SIZE_IN_BYTES + "}")
    private long maxUploadSizeInBytes;

    public long getMaxUploadSizeInBytes() {
        return maxUploadSizeInBytes;
    }

}