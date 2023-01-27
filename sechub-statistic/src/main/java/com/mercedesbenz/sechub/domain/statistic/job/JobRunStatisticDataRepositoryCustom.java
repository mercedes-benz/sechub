package com.mercedesbenz.sechub.domain.statistic.job;

import java.util.Optional;
import java.util.UUID;

public interface JobRunStatisticDataRepositoryCustom {

    public Optional<JobRunStatisticData> findByExecutionUUID(UUID executionUUID);
}
