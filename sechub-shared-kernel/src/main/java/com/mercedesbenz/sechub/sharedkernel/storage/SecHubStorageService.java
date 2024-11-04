// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.storage;

import java.util.UUID;

import com.mercedesbenz.sechub.commons.core.util.SecHubStorageUtil;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.storage.core.StorageService;

public interface SecHubStorageService extends StorageService {

    public default JobStorage createJobStorageForProject(String projectId, UUID jobUUID) {

        String storagePath = SecHubStorageUtil.createStoragePathForProject(projectId);

        return createJobStorageForPath(storagePath, jobUUID);
    }
}
