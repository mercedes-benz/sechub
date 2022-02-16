// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.util.UUID;

import com.daimler.sechub.integrationtest.internal.IntegrationTestPDSJobStatus;

public class AssertPDSResult {

    private IntegrationTestPDSJobStatus status;

    public AssertPDSResult(String json) {
        status = IntegrationTestPDSJobStatus.fromJson(json);
    }

    public AssertPDSResult isInState(String state) {
        assertEquals(state, status.state);
        return this;
    }

    public AssertPDSResult hasJobUUID() {
        assertNotNull(status.jobUUID);
        return this;
    }

    public UUID getJobUUID() {
        return status.jobUUID;
    }
}
