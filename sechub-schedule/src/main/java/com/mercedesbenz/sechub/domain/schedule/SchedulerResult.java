// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.model.JSONable;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchedulerResult implements JSONable<SchedulerResult> {

    public static final String PROPERTY_JOBID = "jobId";

    UUID jobId;

    public SchedulerResult(UUID jobId) {
        this.jobId = jobId;
    }

    public UUID getJobId() {
        return jobId;
    }

    @Override
    public Class<SchedulerResult> getJSONTargetClass() {
        return SchedulerResult.class;
    }

}
