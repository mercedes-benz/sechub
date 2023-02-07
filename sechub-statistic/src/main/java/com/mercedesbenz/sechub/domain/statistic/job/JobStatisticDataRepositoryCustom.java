package com.mercedesbenz.sechub.domain.statistic.job;

import java.util.List;
import java.util.UUID;

public interface JobStatisticDataRepositoryCustom {

    public List<JobStatisticData> findAllBySechubJobUUID(UUID sechubJobUUID);
}
