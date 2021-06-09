// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.storage;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.daimler.sechub.pds.PDSProfiles;
import com.daimler.sechub.storage.core.JobStorage;

@Component
@Profile("!" + PDSProfiles.INTEGRATIONTEST)
public class DefaultPDSStorageInfoCollector implements PDSStorageInfoCollector {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPDSStorageInfoCollector.class);

    @Override
    public void informFetchedStorage(String storagePath, UUID sechubJobUUID, UUID pdsJobUUID, JobStorage storage) {
        LOG.debug("fetched storagepath:{} for sechubJobUUID:{}, pdsJobUUID:{}, storage:{}", storagePath, sechubJobUUID, pdsJobUUID,
                storage == null ? null : storage.getClass());

    }

}
