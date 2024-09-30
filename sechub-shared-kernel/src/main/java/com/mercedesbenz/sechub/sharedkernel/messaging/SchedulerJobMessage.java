// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;
import com.mercedesbenz.sechub.commons.model.JSONable;

/**
 * This message data object contains all possible information about a scheduler
 * job
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@MustBeKeptStable("This configuration is used by communication between schedule domain and administration - and maybe others")
public class SchedulerJobMessage implements JSONable<SchedulerJobMessage> {

    private UUID sechubJobUUID;
    private boolean cancelRequested;
    private boolean existing;
    private boolean ended;
    private boolean initializing;
    private boolean readyToStart;
    private boolean started;

    private boolean canceled;
    private boolean suspended;

    @Override
    public Class<SchedulerJobMessage> getJSONTargetClass() {
        return SchedulerJobMessage.class;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setSecHubJobUUID(UUID jobUUID) {
        this.sechubJobUUID = jobUUID;
    }

    public UUID getSechubJobUUID() {
        return sechubJobUUID;
    }

    public void setCancelRequested(boolean cancelReqeusted) {
        this.cancelRequested = cancelReqeusted;
    }

    public boolean isCancelRequested() {
        return cancelRequested;
    }

    public void setExisting(boolean exists) {
        this.existing = exists;
    }

    public boolean isExisting() {
        return existing;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }

    public boolean isEnded() {
        return ended;
    }

    public void setInitializing(boolean initializing) {
        this.initializing = initializing;
    }

    public boolean isInitializing() {
        return initializing;
    }

    public void setReadyToStart(boolean readyToStart) {
        this.readyToStart = readyToStart;
    }

    public boolean isReadyToStart() {
        return readyToStart;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isStarted() {
        return started;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public boolean isSuspended() {
        return suspended;
    }

}
