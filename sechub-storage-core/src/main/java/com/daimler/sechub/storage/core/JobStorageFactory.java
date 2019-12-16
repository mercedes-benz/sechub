// SPDX-License-Identifier: MIT
package com.daimler.sechub.storage.core;

import java.util.UUID;

public interface JobStorageFactory {

	/**
	 * Creates a new job storage for given project and job
	 * @param projectId
	 * @param jobUUID
	 * @return job storage, never <code>null</code>
	 */
	public JobStorage createJobStorage(String projectId, UUID jobUUID);
}
