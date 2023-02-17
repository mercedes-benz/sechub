// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.statistic.job;

import java.util.List;
import java.util.UUID;

public interface JobRunStatisticDataRepositoryCustom {

    public List<JobRunStatisticData> findAllByExecutionUUID(UUID sechubJobUUID);
}
