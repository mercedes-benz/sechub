// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario5;

import static com.daimler.sechub.integrationtest.scenario5.Scenario5.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestProject;

/**
 * Integration tests between int test sechub server and integration test PDS server
 * 
 * @author Albert Tregnaghi
 *
 */
public class PDSCodeScanJobIntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario5.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;


    @Test
    public void implement_me() {
        // at the moment this test does only fail when no PDS server starts - thats all
        /* TODO Albert Tregnaghi, 2020-07-17: implement this when starting to integrate PDS into SecHub server */
    }

    
}
