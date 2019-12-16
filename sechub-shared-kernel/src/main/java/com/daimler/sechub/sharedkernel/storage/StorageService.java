// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.storage;
import java.util.UUID;

import com.daimler.sechub.storage.core.JobStorage;

public interface StorageService {

   public JobStorage getJobStorage(String projectId, UUID jobUUID);

}