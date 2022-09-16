// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import java.util.UUID;

public class AssertPDSJob {

    private UUID pdsJobUUID;

    public static AssertPDSJob assertPDSJob(UUID pdsJobUUID) {
        return new AssertPDSJob(pdsJobUUID);
    }

    private AssertPDSJob(UUID pdsJobUUID) {
        this.pdsJobUUID = pdsJobUUID;
    }

    public AssertPDSJob containsVariableTestOutput(String variableName, Object expectedValue) {
        if (expectedValue == null) {
            expectedValue = "";
        }
        return containsVariableTestOutput(variableName, expectedValue.toString());
    }

    public AssertPDSJob containsVariableTestOutput(String variableName, String expectedValue) {
        String outputStreamText = asPDSUser(PDS_ADMIN).getJobOutputStreamText(pdsJobUUID);
        assertNotNull(outputStreamText);

        String expectedToBeContained = ">" + variableName + "=" + expectedValue;

        if (!outputStreamText.contains(expectedToBeContained)) {
            // we use equals methods to have the compare inside IDE. Easier to find
            // similarities etc.
            assertEquals("Searched for:\n\n" + expectedToBeContained, "But output did only contain:\n\n" + outputStreamText);
        }
        return this;
    }

}
