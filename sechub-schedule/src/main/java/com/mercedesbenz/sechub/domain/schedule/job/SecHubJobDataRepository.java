// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SecHubJobDataRepository extends JpaRepository<ScheduleSecHubJobData, UUID> {

    @Transactional
    @Modifying
    @Query(ScheduleSecHubJobData.QUERY_DELETE_JOB_DATA_OLDER_THAN)
    public int deleteJobDataOlderThan(@Param("cleanTimeStamp") LocalDateTime cleanTimeStamp);

}
