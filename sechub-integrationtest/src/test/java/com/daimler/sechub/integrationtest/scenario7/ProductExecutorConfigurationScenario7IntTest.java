// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario7;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestExecutorProductIdentifier;

public class ProductExecutorConfigurationScenario7IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario7.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(30);

    @Test
    public void an_admin_can_create_a_new_product_executor_config_and_it_returns_uuid() {
        
        /* execute */
        UUID uuid = as(SUPER_ADMIN).createProductExecutorConfig(TestExecutorProductIdentifier.PDS_CODESCAN, 1, "pds gosec-1");
        
        /* test */
        assertNotNull(uuid);

    }

}
