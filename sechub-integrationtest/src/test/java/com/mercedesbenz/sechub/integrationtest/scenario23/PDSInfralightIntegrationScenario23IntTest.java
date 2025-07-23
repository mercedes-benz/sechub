package com.mercedesbenz.sechub.integrationtest.scenario23;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario23.Scenario23.*;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
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
        SecHubConfigurationModel model = createInfraScanTestModelFor(PROJECT_1);
        /* @formatter:off */
        UUID jobUUID = as(USER_1).
                withSecHubClient(new File("src/test/resources/solution-mocks")).
                startAsynchronScanFor(PROJECT_1, model).
                getJobUUID();
        /* @formatter:on */

        waitForJobDone(PROJECT_1, jobUUID, 30, true);
    }

    private SecHubConfigurationModel createInfraScanTestModelFor(TestProject project1) {
        SecHubInfrastructureScanConfiguration infraScan = new SecHubInfrastructureScanConfiguration();
        try {
            infraScan.getUris().add(new URI("https://example.com"));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Should not happen - test corrupt", e);
        }
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setInfraScan(infraScan);
        
        return model;
    }

}
