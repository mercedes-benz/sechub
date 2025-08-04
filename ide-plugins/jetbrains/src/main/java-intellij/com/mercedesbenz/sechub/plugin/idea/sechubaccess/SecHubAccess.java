// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.sechubaccess;

import com.mercedesbenz.sechub.api.internal.gen.model.ProjectData;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubJobInfoForUserListPage;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubReport;
import com.mercedesbenz.sechub.api.internal.gen.model.*;

import java.util.List;
import java.util.UUID;

public interface SecHubAccess {
    boolean isSecHubServerAlive();

    List<ProjectData> getSecHubProjects();

    boolean isProjectIdDeprecated(String projectId);

    SecHubJobInfoForUserListPage getSecHubJobPage(String projectId, int size, int page);

    SecHubReport getSecHubReport(String projectId, UUID jobUUID);

    FalsePositiveProjectConfiguration getFalsePositiveProjectConfiguration(String projectId);

    void markFalsePositive(String projectId, FalsePositives falsePositives);
}
