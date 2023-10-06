// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiJobStatus;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.commons.model.job.ExecutionResult;
import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.commons.model.job.JobStatusInfo;

public class JobStatus extends JobStatusInfo {

    static JobStatus from(OpenApiJobStatus status) {

        JobStatus result = new JobStatus();

        result.setCreated(asTime(status.getCreated()));
        result.setStarted(asTime(status.getStarted()));
        result.setEnded(asTime(status.getEnded()));

        result.setJobUUID(asUUID(status.getJobUUID()));
        result.setOwner(status.getOwner());
        result.setTrafficLight(TrafficLight.fromString(status.getTrafficLight()));

        result.setResult(ExecutionResult.fromString(status.getResult()));
        result.setState(ExecutionState.fromString(status.getState()));

        return result;
    }

    private static UUID asUUID(String jobUUID) {
        if (jobUUID == null) {
            return null;
        }
        return UUID.fromString(jobUUID);
    }

    private static LocalDateTime asTime(String time) {
        if (time == null || time.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(time);
        } catch (DateTimeParseException pe) {
            throw new RuntimeException("Was not able to parse time:" + time, pe);
        }
    }

}
