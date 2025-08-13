package com.mercedesbenz.sechub.plugin.idea.falsepositive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

import static java.util.Objects.requireNonNull;

public class FalsePositive {
    private static final String JOB_UUID_PROPERTY = "jobUUID";
    private static final String FINDING_ID_PROPERTY = "findingId";

    private final UUID jobUUID;
    private final int findingId;
    private String comment;

    @JsonCreator
    public FalsePositive(@JsonProperty(JOB_UUID_PROPERTY) UUID jobUUID, @JsonProperty(FINDING_ID_PROPERTY) int findingId) {
        this.jobUUID = requireNonNull(jobUUID, "Property 'jobUUID' must not be null");
        if (findingId <= 0) {
            throw new IllegalArgumentException("Property 'findingId' must be greater than 0");
        }
        this.findingId = findingId;
    }

    public UUID getJobUUID() {
        return jobUUID;
    }

    public int getFindingId() {
        return findingId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
