package com.mercedesbenz.sechub.plugin.idea.sechubaccess;

import com.mercedesbenz.sechub.api.internal.gen.model.ProjectData;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubJobInfoForUserListPage;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubReport;

import java.util.List;
import java.util.UUID;

public interface SecHubAccess {
    boolean isSecHubServerAlive();

    List<ProjectData> getSecHubProjects();

    SecHubJobInfoForUserListPage getSecHubJobPage(String projectId, int size, int page);

    SecHubReport getSecHubReport(String projectId, UUID jobUUID);
}
