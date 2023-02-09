package com.mercedesbenz.sechub.domain.statistic.job;

import java.util.List;
import java.util.UUID;

public interface JobRunStatisticRepositoryCustom {

    List<JobRunStatistic> findAllBySechubJobUUID(UUID sechubJobUUID);
}
