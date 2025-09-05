package com.mercedesbenz.sechub.integrationtest.scenario23;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario23.Scenario23.*;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubInfrastructureScanConfiguration;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestExtension;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.WithTestScenario;

@ExtendWith(IntegrationTestExtension.class)
@WithTestScenario(Scenario23.class)
class PDSInfralightIntegrationScenario23IntTest {

    @Test
    void pds_solution_infralight_mocked() throws Exception {
        /* prepare */
        TestProject projectId = PROJECT_1;
        
        SecHubConfigurationModel model = createInfraScanTestModelFor(projectId);
        
        as(SUPER_ADMIN).updateWhiteListForProject(projectId, List.of("https://example.com"));
        
        /* @formatter:off */
        UUID jobUUID = as(USER_1).
                withSecHubClient(new File("src/test/resources/solution-mocks")).
                startAsynchronScanFor(projectId, model).
                getJobUUID();
        /* @formatter:on */

        waitForJobDone(projectId, jobUUID, 30, true);
        
        /* test 1 */
        String secretScanReport = as(USER_1).getJobReport(projectId, jobUUID);
        storeTestReport("report_pds_infrascan_infralight-1.json", secretScanReport);
        
        String htmlReport = as(USER_1).
                enableAutoDumpForHTMLReports().
                getHTMLJobReport(projectId, jobUUID);
        storeTestReport("report_pds_infrascan_infralight-1.html", htmlReport);

    }

    private SecHubConfigurationModel createInfraScanTestModelFor(TestProject project1) {
        SecHubInfrastructureScanConfiguration infraScan = new SecHubInfrastructureScanConfiguration();
        try {
            infraScan.getUris().add(new URI("https://example.com"));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Should not happen - test corrupt", e);
        }
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setApiVersion("1.0");
        model.setInfraScan(infraScan);
        
        return model;
    }

}
