// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;
import com.mercedesbenz.sechub.commons.model.JSONable;

/**
 * This message data object contains all possible information about a project
 *
 * @author Albert Tregnaghi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@MustBeKeptStable("This configuration is used by communication between assistent domain and scan")
public class JobFinding implements JSONable<JobFinding> {

    private String projectId;
    private UUID jobUUID;
    private int findingId;

    private Integer cweId;
    private String fileName;
    private String relevantSource;
    private boolean available;
    private String findingName;
    private String findingDescription;

    @Override
    public Class<JobFinding> getJSONTargetClass() {
        return JobFinding.class;
    }

    public void setJobUUID(UUID jobUUID) {
        this.jobUUID = jobUUID;
    }

    public UUID getJobUUID() {
        return jobUUID;
    }

    public int getFindingId() {
        return findingId;
    }

    public void setFindingId(int findingId) {
        this.findingId = findingId;
    }

    public Integer getCweId() {
        return cweId;
    }

    public void setCweId(Integer cweId) {
        this.cweId = cweId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getRelevantSource() {
        return relevantSource;
    }

    public void setRelevantSource(String relevantSource) {
        this.relevantSource = relevantSource;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean accessGranted) {
        this.available = accessGranted;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getFindingName() {
        return findingName;
    }

    public void setFindingName(String findingName) {
        this.findingName = findingName;
    }

    public String getFindingDescription() {
        return findingDescription;
    }

    public void setFindingDescription(String findingDescription) {
        this.findingDescription = findingDescription;
    }

}
