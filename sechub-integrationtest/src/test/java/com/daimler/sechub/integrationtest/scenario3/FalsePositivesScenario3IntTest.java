// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario3;

import static com.daimler.sechub.integrationtest.api.AssertSecHubReport.*;
import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario3.Scenario3.*;
import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.commons.model.TrafficLight;
import com.daimler.sechub.integrationtest.api.AsUser.ProjectFalsePositivesDefinition;
import com.daimler.sechub.integrationtest.api.IntegrationTestJSONLocation;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestProject;
import com.daimler.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;
public class FalsePositivesScenario3IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario3.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;

    
    
    @Test
    public void with_sechubclient_mark_falsepositives_of_only_existing_medium_will_result_in_report_without_defined__And_trafficlight_changes_from_yellow_to_green() throws Exception {
        /* @formatter:off */
        /***********/
        /* prepare */
        /***********/
        IntegrationTestJSONLocation location = IntegrationTestJSONLocation.CLIENT_JSON_SOURCESCAN_YELLOW;
        ExecutionResult result = as(USER_1).withSecHubClient().startSynchronScanFor(project, location);
        assertSecHubReport(result).
            finding().id(1).name("Absolute Path Traversal").isContained().
            hasTrafficLight(TrafficLight.YELLOW);

        UUID jobUUID = result.getSechubJobUUID();

        /***********/
        /* execute */
        /***********/
        as(USER_1).withSecHubClient().startFalsePositiveDefinition(project,location).add(1, jobUUID).markAsFalsePositive();

        /********/
        /* test */
        /********/
        ExecutionResult result2 = as(USER_1).withSecHubClient().startSynchronScanFor(project, location);
        assertSecHubReport(result2).
            finding().id(1).name("Absolute Path Traversal").isNotContained().
            hasTrafficLight(TrafficLight.GREEN);

        /* @formatter:on */
    }
    
    @Test
    public void REST_API_direct_mark_falsepositives_of_only_existing_medium_will_result_in_report_without_defined__And_trafficlight_changes_from_yellow_to_green() throws Exception {
        /* @formatter:off */
        /***********/
        /* prepare */
        /***********/
        IntegrationTestJSONLocation location = IntegrationTestJSONLocation.CLIENT_JSON_SOURCESCAN_YELLOW;
        ExecutionResult result = as(USER_1).withSecHubClient().startSynchronScanFor(project, location);
        assertSecHubReport(result).
            finding().id(1).name("Absolute Path Traversal").isContained().
            hasTrafficLight(TrafficLight.YELLOW);

        UUID jobUUID = result.getSechubJobUUID();

        /***********/
        /* execute */
        /***********/
        as(USER_1).startFalsePositiveDefinition(project).add(1, jobUUID).markAsFalsePositive();

        /********/
        /* test */
        /********/
        ExecutionResult result2 = as(USER_1).withSecHubClient().startSynchronScanFor(project, location);
        assertSecHubReport(result2).
            finding().id(1).name("Absolute Path Traversal").isNotContained().
            hasTrafficLight(TrafficLight.GREEN);

        /* @formatter:on */
    }
    
    @Test
    public void REST_API_direct_mark_20_false_positives_with_comments_is_accepted() throws Exception {
        /* @formatter:off */
        
        /***********/
        /* prepare */
        /***********/
        IntegrationTestJSONLocation location = IntegrationTestJSONLocation.CLIENT_JSON_SOURCESCAN_YELLOW;
        ExecutionResult result = as(USER_1).withSecHubClient().startSynchronScanFor(project, location);
        UUID jobUUID = result.getSechubJobUUID();

        /***********/
        /* execute */
        /***********/
        ProjectFalsePositivesDefinition def = as(USER_1).
            startFalsePositiveDefinition(project);
        
        int loops = 20;
        
        for (int i=1;i<loops;i++) {
            def.add(i, jobUUID, "comment for loop:"+i);
        }
        def.markAsFalsePositive();
        
        /********/
        /* test */
        /********/
        ProjectFalsePositivesDefinition configuration = as(USER_1).getFalsePositiveConfigurationOfProject(project);
        configuration.isContaining(loops-1, jobUUID);
        
        /* @formatter:on */
    }
    
    @Test
    public void REST_API_direct_unmark_falsepositives_of_only_existing_medium_will_result_in_report_without_defined__And_trafficlight_changes_from_gren_to_yellow() throws Exception {
        /* @formatter:off */
        /***********/
        /* prepare */
        /***********/
        IntegrationTestJSONLocation location = IntegrationTestJSONLocation.CLIENT_JSON_SOURCESCAN_YELLOW;
        ExecutionResult result = as(USER_1).withSecHubClient().startSynchronScanFor(project, location);
        UUID jobUUID = result.getSechubJobUUID();

        as(USER_1).startFalsePositiveDefinition(project).add(1, jobUUID).markAsFalsePositive();

        // create scan + fetch report again (check filtering of false positive works as a precondition */
        ExecutionResult result2 = as(USER_1).withSecHubClient().startSynchronScanFor(project, location);
        assertSecHubReport(result2).
            finding().id(1).name("Absolute Path Traversal").isNotContained().
            hasTrafficLight(TrafficLight.GREEN);
        
        /***********/
        /* execute */
        /***********/
        as(USER_1).startFalsePositiveDefinition(project).add(1, jobUUID).unmarkFalsePositive();

        /********/
        /* test */
        /********/

        // create scan + fetch report again
        ExecutionResult result3 = as(USER_1).withSecHubClient().startSynchronScanFor(project, location);
        assertSecHubReport(result3).
            finding().id(1).name("Absolute Path Traversal").isContained().  
            hasTrafficLight(TrafficLight.YELLOW);

        /* @formatter:on */
    }
    
    @Test
    public void with_sechubclient_unmark_falsepositives_of_only_existing_medium_will_result_in_report_without_defined__And_trafficlight_changes_from_gren_to_yellow() throws Exception {
        /* @formatter:off */
        /***********/
        /* prepare */
        /***********/
        IntegrationTestJSONLocation location = IntegrationTestJSONLocation.CLIENT_JSON_SOURCESCAN_YELLOW;
        ExecutionResult result = as(USER_1).withSecHubClient().startSynchronScanFor(project, location);
        UUID jobUUID = result.getSechubJobUUID();

        as(USER_1).startFalsePositiveDefinition(project).add(1, jobUUID).markAsFalsePositive();

        // create scan + fetch report again (check filtering of false positive works as a precondition */
        ExecutionResult result2 = as(USER_1).withSecHubClient().startSynchronScanFor(project, location);
        assertSecHubReport(result2).
            finding().id(1).name("Absolute Path Traversal").isNotContained().
            hasTrafficLight(TrafficLight.GREEN);
        
        /***********/
        /* execute */
        /***********/
        as(USER_1).withSecHubClient().startFalsePositiveDefinition(project,location).add(1, jobUUID).unmarkFalsePositive();

        /********/
        /* test */
        /********/

        // create scan + fetch report again
        ExecutionResult result3 = as(USER_1).withSecHubClient().startSynchronScanFor(project, location);
        assertSecHubReport(result3).
            finding().id(1).name("Absolute Path Traversal").isContained().
            hasTrafficLight(TrafficLight.YELLOW);

        /* @formatter:on */
    }

    @Test
    public void REST_API_direct_fetch_fp_config_when_one_entry_added() throws Exception {
        /* @formatter:off */
        /***********/
        /* prepare */
        /***********/
        IntegrationTestJSONLocation location = IntegrationTestJSONLocation.CLIENT_JSON_SOURCESCAN_YELLOW;
        ExecutionResult result = as(USER_1).withSecHubClient().startSynchronScanFor(project, location);
        assertSecHubReport(result).
            finding().id(1).name("Absolute Path Traversal").isContained().
            hasTrafficLight(TrafficLight.YELLOW);

        UUID jobUUID = result.getSechubJobUUID();

        as(USER_1).startFalsePositiveDefinition(project).add(1, jobUUID).markAsFalsePositive();

        /***********/
        /* execute */
        /***********/
        ProjectFalsePositivesDefinition configuration = as(USER_1).getFalsePositiveConfigurationOfProject(project);

        /********/
        /* test */
        /********/
        assertTrue(configuration.isContaining(1, jobUUID));
        
        /* @formatter:on */
    }
    
    @Test
    public void with_sechubclient_fetch_fp_config_when_one_entry_added() throws Exception {
        /* @formatter:off */
        /***********/
        /* prepare */
        /***********/
        IntegrationTestJSONLocation location = IntegrationTestJSONLocation.CLIENT_JSON_SOURCESCAN_YELLOW;
        ExecutionResult result = as(USER_1).withSecHubClient().startSynchronScanFor(project, location);
        assertSecHubReport(result).
            finding().id(1).name("Absolute Path Traversal").isContained().
            hasTrafficLight(TrafficLight.YELLOW);

        UUID jobUUID = result.getSechubJobUUID();

        as(USER_1).startFalsePositiveDefinition(project).add(1, jobUUID).markAsFalsePositive();

        /***********/
        /* execute */
        /***********/

        ProjectFalsePositivesDefinition configuration = as(USER_1).withSecHubClient().getFalsePositiveConfigurationOfProject(project,location);
        
        /********/
        /* test */
        /********/
        assertTrue(configuration.isContaining(1, jobUUID));
        
        /* @formatter:on */
    }
    

}
