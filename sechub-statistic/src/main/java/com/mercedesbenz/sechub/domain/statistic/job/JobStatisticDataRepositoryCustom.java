package com.mercedesbenz.sechub.domain.statistic.job;

import java.util.Optional;
import java.util.UUID;

public interface JobStatisticDataRepositoryCustom {

    public Optional<JobStatisticData> findBySechubJobUUID(UUID sechubJobUUID);
}
