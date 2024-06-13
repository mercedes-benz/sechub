// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.storage;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.pds.commons.core.PDSProfiles;
import com.mercedesbenz.sechub.storage.core.JobStorage;

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
