// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.batch;

import static com.daimler.sechub.domain.schedule.SchedulingConstants.*;

import java.util.UUID;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.util.Assert;

@Component
public class SecHubBatchJobParameterBuilder {

    public JobParameters buildParams(UUID sechubJobUUID) {
        Assert.notNull(sechubJobUUID, "sechub job UUID may not be null!");

        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addString(BATCHPARAM_SECHUB_UUID, sechubJobUUID.toString());
        builder.addString("random", UUID.randomUUID().toString());

        /* prepare batch job */
        JobParameters jobParameters = builder.toJobParameters();
        return jobParameters;
    }
}
