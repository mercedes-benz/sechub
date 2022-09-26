// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class AssertPDSJob {

    private UUID pdsJobUUID;
    private Map<String, String> cachedVariableMap;

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
        if (cachedVariableMap == null) {
            cachedVariableMap = TestAPI.fetchPDSVariableTestOutputMap(pdsJobUUID);
        }

        String variableValue = cachedVariableMap.get(variableName);

        if (!Objects.equals(expectedValue, variableValue)) {
            if (variableValue == null) {
                String expectedToBeContained = ">" + variableName + "=" + expectedValue;
                String outputStreamText = asPDSUser(PDS_ADMIN).getJobOutputStreamText(pdsJobUUID);
                // we use equals methods to have the compare inside IDE. Easier to find
                // similarities etc.
                assertEquals("Searched for:\n\n" + expectedToBeContained, "But output did only contain:\n\n" + outputStreamText);

            } else {
                assertEquals(expectedValue, variableValue);
            }
        }
        return this;
    }

}
