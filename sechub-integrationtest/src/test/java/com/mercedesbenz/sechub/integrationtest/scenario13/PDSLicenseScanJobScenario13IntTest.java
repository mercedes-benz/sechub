package com.mercedesbenz.sechub.integrationtest.scenario13;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.as;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.waitForJobDone;
import static com.mercedesbenz.sechub.integrationtest.scenario13.Scenario13.PROJECT_1;
import static com.mercedesbenz.sechub.integrationtest.scenario13.Scenario13.USER_1;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;

import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestFileSupport;

public class PDSLicenseScanJobScenario13IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario13.class);
    
    @Test
    public void pds_license_scan() {
        /* prepare */
        String configurationAsJson = IntegrationTestFileSupport.getTestfileSupport().loadTestFile("sechub-integrationtest-licensescanconfig.json");
        SecHubScanConfiguration configuration = SecHubScanConfiguration.createFromJSON(configurationAsJson);
        
        configuration.setProjectId("myTestProject");
        
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createJobAndReturnJobUUID(project, configuration);
        
        /* execute */
        as(USER_1).
            approveJob(project, jobUUID);

        waitForJobDone(project, jobUUID, 30, true);
        
        /* test */
        String spdxReport = as(USER_1).getSpdxReport(project, jobUUID);
        assertEquals("abc", spdxReport);
                
    }
}
