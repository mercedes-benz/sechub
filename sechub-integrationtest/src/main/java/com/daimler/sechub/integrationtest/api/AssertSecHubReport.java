// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import com.daimler.sechub.commons.model.TrafficLight;
import com.daimler.sechub.integrationtest.JSONTestSupport;
import com.daimler.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;
import com.daimler.sechub.test.TestFileSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class AssertSecHubReport {
    private static String lastOutputLIne;
    private JSONTestSupport jsonTestSupport = JSONTestSupport.DEFAULT;
    private JsonNode jsonObj;

    private AssertSecHubReport(String json) {
        try {
            jsonObj = jsonTestSupport.fromJson(json);
        } catch (IOException e) {
            throw new RuntimeException("Not able to read json obj", e);
        }
    }

    public static AssertSecHubReport assertSecHubReport(String json) {
        return new AssertSecHubReport(json);
    }

    public static AssertSecHubReport assertSecHubReport(ExecutionResult result) {
        lastOutputLIne = result.getLastOutputLine();
        File file = result.getJSONReportFile();
        if (!file.exists()) {
            fail("No report file found:"+file.getAbsolutePath()+"\nLast output line was:" + lastOutputLIne);
        }
        String json = TestFileSupport.loadTextFile(file, "\n");
        return assertSecHubReport(json);
    }

    public AssertSecHubReport containsFinding(int findingId, String findingName) {
        assertReportContainsFindingOrNot(findingId, findingName, true);
        return this;
    }

    public AssertSecHubReport containsNotFinding(int findingId, String findingName) {
        assertReportContainsFindingOrNot(findingId, findingName, false);
        return this;
    }

    public AssertSecHubReport hasTrafficLight(TrafficLight trafficLight) {
        JsonNode r = jsonObj.get("trafficLight");
        if (r == null) {
            fail("No trafficlight found inside report!\nLast output line was:" + lastOutputLIne);
        }
        String trText = r.asText();
        assertEquals(trafficLight, TrafficLight.fromString(trText));
        return this;
    }

    private void assertReportContainsFindingOrNot(int findingId, String findingName, boolean expectedToBeFound) {
        JsonNode r = jsonObj.get("result");
        JsonNode f = r.get("findings");
        ArrayNode findings = (ArrayNode) f;
        JsonNode found = null;
        for (int i = 0; i < findings.size(); i++) {
            JsonNode finding = findings.get(i);

            String foundName = finding.get("name").asText();
            int foundFindingId = finding.get("id").asInt();

            if (!foundName.equals(findingName)) {
                continue;
            }
            if (foundFindingId != findingId) {
                continue;
            }
            found = finding;
            break;
        }
        if (found == null && expectedToBeFound) {
            fail("Not found finding");
        } else if (found != null && !expectedToBeFound) {
            fail("Did found entry:" + found.toPrettyString());
        }
    }
}
