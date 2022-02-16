// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.storage;

import java.util.UUID;

import com.mercedesbenz.sechub.storage.core.JobStorage;

public interface PDSStorageInfoCollector {

    public void informFetchedStorage(String storagePath, UUID sechubJobUUID, UUID pdsJobUUID, JobStorage storage);
}
