// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.job;

import java.util.Optional;

public interface SecHubJobRepositoryCustom {

	/**
	 * @return next executable job as optional - check if present or not is
	 *         necessary
	 */
	Optional<ScheduleSecHubJob> findNextJobToExecute();
}
