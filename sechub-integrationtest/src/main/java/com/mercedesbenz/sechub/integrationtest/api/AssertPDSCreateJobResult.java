// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.util.UUID;

import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestPDSJobCreateResult;

public class AssertPDSCreateJobResult {

    private IntegrationTestPDSJobCreateResult status;

    public AssertPDSCreateJobResult(String json) {
        status = IntegrationTestPDSJobCreateResult.fromJson(json);
    }

    public AssertPDSCreateJobResult hasJobUUID() {
        assertNotNull(status.jobUUID);
        return this;
    }

    public UUID getJobUUID() {
        return status.jobUUID;
    }
}
