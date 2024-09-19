// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;

@Component
public class SchedulerBinariesUploadConfiguration {

    private static final long DEFAULT_MAX_UPLOAD_SIZE_IN_BYTES = 50 * 1024 * 1024; // 50 MiB

    @MustBeDocumented("Define the maximum amount of bytes accepted for uploading `" + FILENAME_BINARIES_TAR + "`. The default when not set is "
            + DEFAULT_MAX_UPLOAD_SIZE_IN_BYTES + " (" + (DEFAULT_MAX_UPLOAD_SIZE_IN_BYTES / 1024 / 1024) + " MiB)")
    @Value("${sechub.upload.binaries.maximum.bytes:" + DEFAULT_MAX_UPLOAD_SIZE_IN_BYTES + "}")
    private long maxUploadSizeInBytes;

    public long getMaxUploadSizeInBytes() {
        return maxUploadSizeInBytes;
    }

}