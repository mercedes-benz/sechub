// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.storage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.daimler.sechub.pds.PDSProfiles;
import com.daimler.sechub.storage.core.JobStorage;

@Component
@Profile(PDSProfiles.INTEGRATIONTEST)
public class IntegrationTestPDSStorageInfoCollector implements PDSStorageInfoCollector {

    private Map<UUID, String> fetchedJobUUIDStoragePathHistory = new LinkedHashMap<>();

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTestPDSStorageInfoCollector.class);

    @Override
    public void informFetchedStorage(String storagePath, UUID sechubJobUUID, UUID pdsJobUUID, JobStorage storage) {
        LOG.debug("fetched storagepath:{} for sechubJobUUID:{}, pdsJobUUID:{}, storage:{}", storagePath, sechubJobUUID, pdsJobUUID, storage == null ? null : storage.getClass());

        String oldStoragePath = fetchedJobUUIDStoragePathHistory.get(sechubJobUUID);
        if (oldStoragePath != null) {
            if (!storagePath.equals(storagePath)) {
                throw new IllegalStateException("Inside integration test we have situation, that storage fetched for job: " + sechubJobUUID
                        + " was fetched for different pathes:\n1." + oldStoragePath + "\n, 2." + storagePath+"\nTHIS MAY NOT HAPPEN!");
            }
        }
        fetchedJobUUIDStoragePathHistory.put(sechubJobUUID, storagePath);
    }
    
    public Map<UUID, String> getFetchedJobUUIDStoragePathHistory() {
        return fetchedJobUUIDStoragePathHistory;
    }

}
