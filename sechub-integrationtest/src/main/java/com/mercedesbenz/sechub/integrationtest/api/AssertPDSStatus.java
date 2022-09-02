// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.util.UUID;

import com.mercedesbenz.sechub.adapter.pds.data.PDSJobStatus.PDSAdapterJobStatusState;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestPDSJobStatus;

public class AssertPDSStatus {

    private IntegrationTestPDSJobStatus status;

    public AssertPDSStatus(String json) {
        status = IntegrationTestPDSJobStatus.fromJson(json);
    }

    public AssertPDSStatus isInState(PDSAdapterJobStatusState state) {
        return isInState(state.name());
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
