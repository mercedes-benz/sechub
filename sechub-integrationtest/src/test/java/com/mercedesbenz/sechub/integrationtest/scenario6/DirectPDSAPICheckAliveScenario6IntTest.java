// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario6;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;

public class DirectPDSAPICheckAliveScenario6IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario6.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    @Test
    public void anonymous_can_check_alive_state() {
        /* @formatter:off */
        /* execute */
        boolean alive = asPDSUser(ANONYMOUS).getIsAlive();

        /* test */
        assertTrue("PDS server is NOT alive !!! ILLEGAL STATE for tests: So all PDS related integration test will fail", alive);

        /* @formatter:on */
    }

    @Test
    public void pds_techuser_can_check_alive_state() {
        /* @formatter:off */
        /* execute */
        boolean alive = asPDSUser(PDS_TECH_USER).getIsAlive();

        /* test */
        assertTrue("PDS server is NOT alive !!! ILLEGAL STATE for tests: So all PDS related integration test will fail", alive);

        /* @formatter:on */
    }

    @Test
    public void pds_admin_can_check_alive_state() {
        /* @formatter:off */
        /* execute */
        boolean alive = asPDSUser(PDS_ADMIN).getIsAlive();

        /* test */
        assertTrue("PDS server is NOT alive !!! ILLEGAL STATE for tests: So all PDS related integration test will fail", alive);

        /* @formatter:on */
    }

}
