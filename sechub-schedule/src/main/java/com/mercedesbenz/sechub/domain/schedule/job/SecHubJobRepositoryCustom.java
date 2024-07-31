// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

public interface SecHubJobRepositoryCustom {

    Optional<ScheduleSecHubJob> getJob(UUID id);

    Optional<UUID> nextJobIdToExecuteFirstInFirstOut();

    Optional<UUID> nextJobIdToExecuteForProjectNotYetExecuted();

    Optional<UUID> nextJobIdToExecuteForProjectAndModuleGroupNotYetExecuted();

    /**
     * Fetches next jobs which have been canceled or have ended but have an
     * encryption pool entry which is lower (means older) than the given one. The
     * fetched jobs are returned in a random way.
     *
     * @param encryptionPoolId the higher (newer) encryption pool id
     * @param maxAmount        maximum amount of jobs to return
     * @return list of jobs, never <code>null</code>
     */
    List<ScheduleSecHubJob> nextCanceledOrEndedJobsWithEncryptionPoolIdLowerThan(Long encryptionPoolId, int maxAmount);

    @Lock(LockModeType.NONE)
    public long countCanceledOrEndedJobsWithEncryptionPoolIdLowerThan(Long encryptionPoolId);

    /**
     * Collects a distinct list of all encryption pool ids which are used by any job
     * in any state.
     *
     * @return set of encryption pool ids, never <code>null</code>
     */
    List<Long> collectAllUsedEncryptionPoolIdsInsideJobs();
}
