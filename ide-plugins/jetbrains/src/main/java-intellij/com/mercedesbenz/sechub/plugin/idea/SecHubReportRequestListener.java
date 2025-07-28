package com.mercedesbenz.sechub.plugin.idea;

import java.util.UUID;

@FunctionalInterface
public interface SecHubReportRequestListener {
    void onReportRequested(String projectId, UUID jobUUID);
}
