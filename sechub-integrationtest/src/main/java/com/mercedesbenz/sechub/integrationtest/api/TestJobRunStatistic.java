// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
class TestJobRunStatistic {

    public UUID sechubJobUUID;
    public UUID executionUUID;

    public String projectId;

    @Override
    public String toString() {
        return "TestJobRunStatistic [" + (sechubJobUUID != null ? "sechubJobUUID=" + sechubJobUUID + ", " : "")
                + (executionUUID != null ? "executionUUID=" + executionUUID + ", " : "") + (projectId != null ? "projectId=" + projectId : "") + "]";
    }

}