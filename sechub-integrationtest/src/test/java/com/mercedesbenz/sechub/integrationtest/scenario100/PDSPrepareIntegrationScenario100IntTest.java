package com.mercedesbenz.sechub.integrationtest.scenario100;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.Timeout;


class PDSPrepareIntegrationScenario100IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario100.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    @Test
    public void pds_solution_prepare_mock_without_report() throws Exception {
    // TODO

    }
}