// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.sechubaccess;

import com.mercedesbenz.sechub.api.internal.gen.model.ProjectData;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubJobInfoForUserListPage;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubReport;

import java.util.List;
import java.util.UUID;

class NoOpSecHubAccessClient implements SecHubAccess {

    @Override
    public boolean isSecHubServerAlive() {
        return false;
    }

    @Override
    public List<ProjectData> getSecHubProjects() {
        return List.of();
    }

    @Override
    public SecHubJobInfoForUserListPage getSecHubJobPage(String projectId, int size, int page) {
        return null;
    }

    @Override
    public SecHubReport getSecHubReport(String projectId, UUID jobUUID) {
        return null;
    }
}
