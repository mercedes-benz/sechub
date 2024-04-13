// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationMetaData;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.commons.model.job.ExecutionResult;
import com.mercedesbenz.sechub.commons.model.job.ExecutionState;

public class SecHubJobInfoForUser {

    public static final String PROPERTY_JOBUUID = "jobUUID";
    public static final String PROPERTY_CREATED = "created";
    public static final String PROPERTY_STARTED = "started";
    public static final String PROPERTY_ENDED = "ended";
    public static final String PROPERTY_EXECUTED_BY = "executedBy";
    public static final String PROPERTY_EXECUTION_STATE = "executionState";
    public static final String PROPERTY_EXECUTION_RESULT = "executionResult";
    public static final String PROPERTY_TRAFFIC_LIGHT = "trafficLight";
    public static final String PROPERTY_METADATA = "metaData";

    private UUID jobUUID;

    private String executedBy;

    private LocalDateTime created;
    private LocalDateTime started;

    private LocalDateTime ended;

    private ExecutionState executionState;

    private TrafficLight trafficLight;

    private ExecutionResult executionResult;

    private Optional<SecHubConfigurationMetaData> metaData = Optional.empty();

    public UUID getJobUUID() {
        return jobUUID;
    }

    public void setJobUUID(UUID sechubJobUUID) {
        this.jobUUID = sechubJobUUID;
    }

    public String getExecutedBy() {
        return executedBy;
    }

    public void setExecutedBy(String executedBy) {
        this.executedBy = executedBy;
    }

    public LocalDateTime getStarted() {
        return started;
    }

    public void setStarted(LocalDateTime started) {
        this.started = started;
    }

    public LocalDateTime getEnded() {
        return ended;
    }

    public void setEnded(LocalDateTime ended) {
        this.ended = ended;
    }

    public ExecutionResult getExecutionResult() {
        return executionResult;
    }

    public void setExecutionResult(ExecutionResult result) {
        this.executionResult = result;
    }

    public ExecutionState getExecutionState() {
        return executionState;
    }

    public void setExecutionState(ExecutionState status) {
        this.executionState = status;
    }

    public TrafficLight getTrafficLight() {
        return trafficLight;
    }

    public void setTrafficLight(TrafficLight trafficLight) {
        this.trafficLight = trafficLight;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public Optional<SecHubConfigurationMetaData> getMetaData() {
        return metaData;
    }

    public void setMetaData(SecHubConfigurationMetaData metaData) {
        this.metaData = Optional.ofNullable(metaData);
    }

}
