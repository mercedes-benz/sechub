// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import java.util.Optional;
import java.util.UUID;

public interface SecHubJobRepositoryCustom {

    Optional<ScheduleSecHubJob> getJob(UUID id);

    Optional<UUID> nextJobIdToExecuteFirstInFirstOut();

    Optional<UUID> nextJobIdToExecuteForProjectNotYetExecuted();

    Optional<UUID> nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted();
}
