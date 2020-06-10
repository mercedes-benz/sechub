// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario3;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario3.Scenario3.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.integrationtest.JSONTestSupport;
import com.daimler.sechub.integrationtest.api.IntegrationTestJSONLocation;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestProject;
import com.daimler.sechub.integrationtest.internal.IntegrationTestFileSupport;
import com.daimler.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;
import com.daimler.sechub.sharedkernel.type.TrafficLight;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class FalsePositivesScenario3IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario3.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;

    private JSONTestSupport jsonTestSupport = JSONTestSupport.DEFAULT;

    @Test
    public void mark_falsepositives_of_only_existing_medium_will_result_in_report_without_defined__And_trafficlight_changes_from_yellow_to_green() throws Exception {

        /***********/
        /* prepare */
        /***********/

        // create scan + fetch report
        IntegrationTestJSONLocation location = IntegrationTestJSONLocation.CLIENT_JSON_SOURCESCAN_YELLOW;
        ExecutionResult result = as(USER_1).withSecHubClient().startSynchronScanFor(project, location);
        // check precondition - finding found in report...
        assertReportContainsFinding(result, 1, "Absolute Path Traversal");
        assertEquals(TrafficLight.YELLOW, result.getTrafficLight());

        UUID jobUUID = result.getSechubJobUUD();

        /***********/
        /* execute */
        /***********/
        as(USER_1).startFalsePositiveDefinition(project).add(1, jobUUID).sendToServer();
        // mark_false_positive_first_finding

        /********/
        /* test */
        /********/

        // create scan + fetch report again
        ExecutionResult result2 = as(USER_1).withSecHubClient().startSynchronScanFor(project, location);
        assertReportContainsNotFinding(result2, 1, "Absolute Path Traversal");
        assertEquals(TrafficLight.GREEN, result2.getTrafficLight());

    }

    /* -------------------------------------------------------------- */
    /* ------------------------Helpers------------------------------- */
    /* -------------------------------------------------------------- */

    private void assertReportContainsFinding(ExecutionResult result, int findingId, String findingName) throws Exception {
        assertReportContainsFindingOrNot(result, findingId, findingName, true);
    }

    private void assertReportContainsNotFinding(ExecutionResult result, int findingId, String findingName) throws Exception {
        assertReportContainsFindingOrNot(result, findingId, findingName, false);
    }

    private void assertReportContainsFindingOrNot(ExecutionResult result, int findingId, String findingName, boolean expectedToBeFound) throws Exception {
        File file = result.getJSONReportFile();
        String textFile = IntegrationTestFileSupport.getTestfileSupport().loadTextFile(file, "\n");
        JsonNode jsonObj = jsonTestSupport.fromJson(textFile);

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
