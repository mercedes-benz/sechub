// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.util.UUID;

import com.daimler.sechub.integrationtest.internal.IntegrationTestPDSJobStatus;

public class AssertPDSStatus {

    private IntegrationTestPDSJobStatus status;
    
    public AssertPDSStatus(String json) {
        status = IntegrationTestPDSJobStatus.fromJson(json);
    }
    
    public AssertPDSStatus isInState(String state) {
        assertEquals(state, status.state);
        return this;
    }
    
    public AssertPDSStatus hasJobUUID() {
        assertNotNull(status.jobUUID);
        return this;
    }
    
    public UUID getJobUUID() {
        return status.jobUUID;
    }
}
