// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.project;

import java.util.Objects;
import java.util.UUID;

public class FalsePositiveJobData {
    
    public static final String PROPERTY_JOBUUID="jobUUID";
    public static final String PROPERTY_FINDINGID="findingId";
    public static final String PROPERTY_COMMENT="comment";
    
    private UUID jobUUID;
    private int findingId;
    private String comment;
    
    public UUID getJobUUID() {
        return jobUUID;
    }
    
    public void setJobUUID(UUID jobUUID) {
        this.jobUUID = jobUUID;
    }

    public int getFindingId() {
        return findingId;
    }

    public void setFindingId(int id) {
        this.findingId = id;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return "FalsePositiveJobData [jobUUID=" + jobUUID + ", findingId=" + findingId + ", comment=" + comment + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(comment, findingId, jobUUID);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FalsePositiveJobData other = (FalsePositiveJobData) obj;
        return findingId == other.findingId && Objects.equals(jobUUID, other.jobUUID);
    }

}
