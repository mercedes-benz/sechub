// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import java.util.UUID;

import com.mercedesbenz.sechub.commons.archive.ArchiveSupport.ArchivesCreationResult;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;

public interface SecHubClientListener {

    public default void beforeUpload(UUID secHubJobUUID, SecHubConfigurationModel model, ArchivesCreationResult archiveCreationResult) {
        /* do nothing per default */
    }

    public default void afterUpload(UUID secHubJobUUID, SecHubConfigurationModel model, ArchivesCreationResult archiveCreationResult) {
        /* do nothing per default */
    }

    public default void afterReportDownload(UUID jobUUID, SecHubReport report) {
        /* do nothing per default */
    }

}
