// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubConfiguration;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubReport;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;

import java.util.UUID;

public interface SecHubClientListener {

    default void beforeUpload(UUID secHubJobUUID, SecHubConfiguration model, ArchiveSupport.ArchivesCreationResult archiveCreationResult) {
        /* do nothing per default */
    }

    default void afterUpload(UUID secHubJobUUID, SecHubConfiguration model, ArchiveSupport.ArchivesCreationResult archiveCreationResult) {
        /* do nothing per default */
    }

    default void afterReportDownload(UUID jobUUID, SecHubReport report) {
        /* do nothing per default */
    }

}
