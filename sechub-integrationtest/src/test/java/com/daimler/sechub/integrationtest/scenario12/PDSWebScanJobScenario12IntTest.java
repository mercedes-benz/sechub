// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario12;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario12.Scenario12.*;

import java.util.Arrays;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.commons.model.Severity;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestProject;

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

    TestProject project = PROJECT_1;

    @Test
    public void pds_web_scan_has_expected_info_finding_with_given_target_url_and_product2_level_information() {
        /* @formatter:off */

        /* prepare */
        String targetURL = "https://mytargeturl.example.com/app1";
        TestProject project = PROJECT_1;
        as(SUPER_ADMIN).updateWhiteListForProject(PROJECT_1, Arrays.asList(targetURL));
        UUID jobUUID = as(USER_1).createWebScan(PROJECT_1, targetURL);
        
        /* execute */
        as(USER_1).
            approveJob(project, jobUUID);
        
        waitForJobDone(project, jobUUID,30);
        
        /* test */
        String report = as(USER_1).getJobReport(project, jobUUID);
        // the first finding from integrationtest-webscan.sh has always an information inside
        assertReport(report).
            finding(0).
                hasSeverity(Severity.INFO).
                hasDescriptionContaining("PRODUCT2_LEVEL=4711").// this comes from custom mandatory parameter from PDS config
                hasDescriptionContaining("PDS_SCAN_TARGET_URL="+targetURL); // this is a default generated parameter which will always be sent by SecHub without being defined in PDS config!
        
        /* @formatter:on */
    }
    
}
