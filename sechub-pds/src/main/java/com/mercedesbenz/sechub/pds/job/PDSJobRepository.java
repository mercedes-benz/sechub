// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PDSJobRepository extends JpaRepository<PDSJob, UUID>, PDSJobRepositoryCustom {

    @Transactional
    @Modifying
    @Query(PDSJob.QUERY_DELETE_JOB_OLDER_THAN)
    int deleteJobOlderThan(@Param("cleanTimeStamp") LocalDateTime cleanTimeStamp);

}
