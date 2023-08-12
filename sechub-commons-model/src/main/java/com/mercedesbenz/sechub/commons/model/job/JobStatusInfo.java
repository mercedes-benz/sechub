// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.job;

import java.time.LocalDateTime;
import java.util.UUID;

import com.mercedesbenz.sechub.commons.model.TrafficLight;

/**
 * A generic job status information object
 *
 * @author Albert Tregnaghi
 *
 */
public class JobStatusInfo {

    private LocalDateTime created;
    private LocalDateTime started;
    private UUID jobUUID;
    private LocalDateTime ended;
    private String owner;
    private ExecutionResult result;
    private ExecutionState state;
    private TrafficLight trafficLight;

    public JobStatusInfo() {
        /* initialize with defaults */
        this.state = ExecutionState.INITIALIZING;
        this.trafficLight = TrafficLight.OFF;
        this.result = ExecutionResult.NONE;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public LocalDateTime getEnded() {
        return ended;
    }

    public UUID getJobUUID() {
        return jobUUID;
    }

    public String getOwner() {
        return owner;
    }

    public ExecutionResult getResult() {
        return result;
    }

    public LocalDateTime getStarted() {
        return started;
    }

    public ExecutionState getState() {
        return state;
    }

    public TrafficLight getTrafficLight() {
        return trafficLight;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setStarted(LocalDateTime started) {
        this.started = started;
    }

    public void setJobUUID(UUID jobUUID) {
        this.jobUUID = jobUUID;
    }

    public void setEnded(LocalDateTime ended) {
        this.ended = ended;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setResult(ExecutionResult result) {
        this.result = result;
    }

    public void setState(ExecutionState state) {
        this.state = state;
    }

    public void setTrafficLight(TrafficLight trafficLight) {
        this.trafficLight = trafficLight;
    }

}
