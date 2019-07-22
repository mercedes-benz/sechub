// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.storage;
import java.util.UUID;

public interface StorageService {

   public JobStorage getJobStorage(String projectId, UUID jobUUID);

}