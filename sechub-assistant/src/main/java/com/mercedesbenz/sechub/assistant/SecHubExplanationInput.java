// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant;

public class SecHubExplanationInput {

    private String findingName;
    private String findingDescription;
    private Integer cweId;
    private String fileName;
    private String relevantSource;
    private boolean available;

    public String getFindingName() {
        return findingName;
    }

    public String getFindingDescription() {
        return findingDescription;
    }

    public Integer getCweId() {
        return cweId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getRelevantSource() {
        return relevantSource;
    }

    public void setFindingName(String findingName) {
        this.findingName = findingName;
    }

    public void setFindingDescription(String findingDescription) {
        this.findingDescription = findingDescription;
    }

    public void setCweId(Integer cweId) {
        this.cweId = cweId;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setRelevantSource(String relevantSource) {
        this.relevantSource = relevantSource;
    }

    public void setAvailable(boolean accessGranted) {
        this.available = accessGranted;
    }

    public boolean isAvailable() {
        return available;
    }

}
