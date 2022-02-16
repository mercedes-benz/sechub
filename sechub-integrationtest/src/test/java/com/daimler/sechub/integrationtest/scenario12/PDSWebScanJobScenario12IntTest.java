// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario12;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario12.Scenario12.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.commons.model.JSONConverter;
import com.daimler.sechub.commons.model.SecHubScanConfiguration;
import com.daimler.sechub.commons.model.SecHubWebScanConfiguration;
import com.daimler.sechub.commons.model.Severity;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestProject;
import com.daimler.sechub.integrationtest.internal.IntegrationTestFileSupport;

/**
 * Integration test doing code scans by integration test servers (sechub server,
 * pds server)
 * 
 * @author Albert Tregnaghi
 *
 */
public class PDSWebScanJobScenario12IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario12.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    @Test
    public void pds_web_scan_has_expected_info_finding_with_given_target_url_and_product2_level_information_and_sechub_web_config_parts() {
        /* @formatter:off */

        /* prepare */
        String configurationAsJson = IntegrationTestFileSupport.getTestfileSupport().loadTestFile("sechub-integrationtest-webscanconfig-all-options.json");
        SecHubScanConfiguration configuration = SecHubScanConfiguration.createFromJSON(configurationAsJson);
        configuration.setProjectId("myTestProject");
        
        TestProject project = PROJECT_1;
        String targetURL = configuration.getWebScan().get().getUri().toString();
        as(SUPER_ADMIN).updateWhiteListForProject(project, Arrays.asList(targetURL));
        UUID jobUUID = as(USER_1).createJobAndReturnJobUUID(project, configuration);
        
        /* execute */
        as(USER_1).
            approveJob(project, jobUUID);
        
        waitForJobDone(project, jobUUID, 30, true);
        
        /* test */
        String sechubReport = as(USER_1).getJobReport(project, jobUUID);

        // IMPORTANT: The 'integrationtest-webscan.sh' returns the configuration file as part of the resulting report.
        //            It is necessary to start a PDS and SecHub in integration mode. The web scan will be created on the 
        //            SecHub server and SecHub calls the PDS. The PDS in return calls the 'integrationtest-webscan.sh',
        //            which produces the report.
        //
        // Workflow:
        //   This test -- sends webscan config to -> SecHub -- calls -> PDS -- calls -> 'integrationtest-webscan.sh' -- returns -> Report
        //
        // look at 'integrationtest-webscan.sh' for implementation details
        // finding 1: contains target url and more 
        // finding 2: contains sechub configuration (only web parts)
        String descriptionFinding2WithDataInside = assertReport(sechubReport).
            finding(0).
                hasSeverity(Severity.INFO).
                hasDescriptionContaining("PRODUCT2_LEVEL=4711").// this comes from custom mandatory parameter from PDS config
                hasDescriptionContaining("PDS_SCAN_TARGET_URL=" + targetURL). // this is a default generated parameter which will always be sent by SecHub without being defined in PDS config!
            finding(1).
                hasDescriptionContaining("PDS_SCAN_CONFIGURATION={").
                getDescription();
        
        String returndPdsScanConfigurationJSON = 
                 descriptionFinding2WithDataInside.substring("PDS_SCAN_CONFIGURATION=".length());
        /* @formatter:on */

        // the returned JSON must be a valid sechub scan configuration
        SecHubScanConfiguration returnedConfiguration = SecHubScanConfiguration.createFromJSON(returndPdsScanConfigurationJSON);
        assertEquals("ProjectId not as expected", project.getProjectId(), returnedConfiguration.getProjectId());
        assertFalse(targetURL, returnedConfiguration.getCodeScan().isPresent());
        assertFalse(targetURL, returnedConfiguration.getInfraScan().isPresent());
        assertTrue(targetURL, returnedConfiguration.getWebScan().isPresent());

        SecHubWebScanConfiguration webConfiguration = returnedConfiguration.getWebScan().get();
        assertNotNull(webConfiguration.getUri());
        assertEquals(JSONConverter.get().toJSON(configuration, true), JSONConverter.get().toJSON(returnedConfiguration, true));

    }

}
