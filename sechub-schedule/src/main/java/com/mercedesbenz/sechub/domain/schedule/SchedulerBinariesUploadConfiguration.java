// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.DocumentationScopeConstants;

@Component
public class SchedulerBinariesUploadConfiguration {

    private static final long DEFAULT_MAX_UPLOAD_SIZE_IN_BYTES = 50 * 1024 * 1024; // 50 MiB

    @MustBeDocumented(value = "Define the maximum amount of bytes accepted for uploading `" + FILENAME_BINARIES_TAR + "`. The default when not set is "
            + DEFAULT_MAX_UPLOAD_SIZE_IN_BYTES + " (" + (DEFAULT_MAX_UPLOAD_SIZE_IN_BYTES / 1024 / 1024)
            + " MiB)", scope = DocumentationScopeConstants.SCOPE_JOB)
    @Value("${sechub.upload.binaries.maximum.bytes:" + DEFAULT_MAX_UPLOAD_SIZE_IN_BYTES + "}")
    private long maxUploadSizeInBytes;

    public long getMaxUploadSizeInBytes() {
        return maxUploadSizeInBytes;
    }

}