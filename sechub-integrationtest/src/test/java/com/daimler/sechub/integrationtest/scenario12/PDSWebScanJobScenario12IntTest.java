// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario12;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario12.Scenario12.*;

import java.util.Arrays;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

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
    public void pds_web_scan_has_expected_outputstream_text() {
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
        assertReport(report).dump().finding(0).hasDescriptionContaining("PDS_SCAN_TARGET_URL="+targetURL);
    }
    
}
