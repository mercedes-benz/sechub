// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.job;

import java.util.Optional;
import java.util.UUID;

public interface SecHubJobRepositoryCustom {

    /**
     * @return next executable job as optional - check if present or not is
     *         necessary
     */
    Optional<ScheduleSecHubJob> findNextJobToExecute();

    Optional<ScheduleSecHubJob> getJob(UUID id);

    Optional<UUID> nextJobIdToExecuteFirstInFirstOut();

    Optional<UUID> nextJobIdToExecuteForProjectNotYetExecuted();
}
