// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;

public interface PDSJobRepository extends JpaRepository<PDSJob, UUID>, PDSJobRepositoryCustom {

    @Transactional
    @Modifying
    @Query(PDSJob.QUERY_DELETE_JOB_OLDER_THAN)
    int deleteJobOlderThan(@Param("cleanTimeStamp") LocalDateTime cleanTimeStamp);

    @Query(PDSJob.QUERY_FIND_JOBS_IN_STATE)
    List<PDSJob> findAllJobsInState(@Param("statusState") PDSJobStatusState statusState);

    @Transactional
    @Modifying
    @Query(PDSJob.QUERY_FORCE_JOB_STATE_UPDATE)
    void forceStateForJobs(@Param("statusState") PDSJobStatusState statusState, @Param("jobUUIDs") Set<UUID> jobUUIDs);

}
