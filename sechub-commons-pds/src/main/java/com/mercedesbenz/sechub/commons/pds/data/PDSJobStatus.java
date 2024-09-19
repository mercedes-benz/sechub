// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds.data;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * This class represents the schedule job status which can be obtained by REST
 *
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class PDSJobStatus {

    public static final String PROPERTY_JOBUUID = "jobUUID";
    public static final String PROPERTY_OWNER = "owner";
    public static final String PROPERTY_CREATED = "created";
    public static final String PROPERTY_STARTED = "started";
    public static final String PROPERTY_ENDED = "ended";
    public static final String PROPERTY_STATE = "state";
    public static final String PROPERTY_ENCRYPTION_OUT_OF_SYNCH = "encryptionOutOfSync";

    private UUID jobUUID;

    private String owner;

    private String created;
    private String started;
    private String ended;

    private boolean encryptionOutOfSync;

    private PDSJobStatusState state;

    public UUID getJobUUID() {
        return jobUUID;
    }

    public void setJobUUID(UUID jobUUID) {
        this.jobUUID = jobUUID;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getStarted() {
        return started;
    }

    public void setStarted(String started) {
        this.started = started;
    }

    public String getEnded() {
        return ended;
    }

    public void setEnded(String ended) {
        this.ended = ended;
    }

    public PDSJobStatusState getState() {
        return state;
    }

    public void setState(PDSJobStatusState state) {
        this.state = state;
    }

    public boolean isEncryptionOutOfSync() {
        return encryptionOutOfSync;
    }

    public void setEncryptionOutOfSync(boolean encryptionOutOfSync) {
        this.encryptionOutOfSync = encryptionOutOfSync;
    }

}
