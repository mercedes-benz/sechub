package com.mercedesbenz.sechub.integrationtest.api;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
class TestJobStatistic {

    public UUID sechubJobUUID;

    public String projectId;

    @Override
    public String toString() {
        return "TestJobStatistic [" + (sechubJobUUID != null ? "sechubJobUUID=" + sechubJobUUID + ", " : "")
                + (projectId != null ? "projectId=" + projectId : "") + "]";
    }

}