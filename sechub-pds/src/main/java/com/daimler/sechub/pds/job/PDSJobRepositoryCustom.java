// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import java.util.Optional;

public interface PDSJobRepositoryCustom {

	/**
	 * @return next executable job as optional - check if present or not is
	 *         necessary
	 */
	Optional<PDSJob> findNextJobToExecute();
	
	long countJobsOfServerInState(String serverId, PDSJobStatusState state);
}
