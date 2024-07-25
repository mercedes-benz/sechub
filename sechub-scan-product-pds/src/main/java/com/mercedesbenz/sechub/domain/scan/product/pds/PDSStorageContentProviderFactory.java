// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelSupport;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.storage.core.StorageService;

@Component
public class PDSStorageContentProviderFactory {

    @Autowired
    StorageService storageService;

    SecHubConfigurationModelSupport modelSupport = new SecHubConfigurationModelSupport();

    public PDSStorageContentProvider createContentProvider(SecHubExecutionContext context, ReuseSecHubStorageInfoProvider reuseSecHubStorageProvider,
            ScanType scanType) {
        JobStorage storage = null;
        SecHubConfiguration model = context.getConfiguration();

        boolean reuseSecHubStorage = reuseSecHubStorageProvider.isReusingSecHubStorage();

        if (!reuseSecHubStorage) {

            String projectId = model.getProjectId();
            UUID jobUUID = context.getSechubJobUUID();

            storage = storageService.createJobStorage(projectId, jobUUID);
        }

        return new PDSStorageContentProvider(storage, reuseSecHubStorage, scanType, modelSupport, model);
    }

}
