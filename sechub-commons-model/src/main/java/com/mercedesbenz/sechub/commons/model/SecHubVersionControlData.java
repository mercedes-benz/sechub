// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubVersionControlData {

    private String type;
    private String location;
    private SecHubRevisionData revision;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Optional<SecHubRevisionData> getRevision() {
        return Optional.ofNullable(revision);
    }

    public void setRevision(SecHubRevisionData revision) {
        this.revision = revision;
    }

}
