// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.statistic.job;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRunStatisticDataRepository extends JpaRepository<JobRunStatisticData, UUID>, JobRunStatisticDataRepositoryCustom {

}
