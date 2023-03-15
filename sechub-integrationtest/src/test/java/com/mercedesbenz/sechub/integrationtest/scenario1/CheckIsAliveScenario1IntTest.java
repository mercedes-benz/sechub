// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario1;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;

public class CheckIsAliveScenario1IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario1.class);

    /* +-----------------------------------------------------------------------+ */
    /* +............................ Get server version .......................+ */
    /* +-----------------------------------------------------------------------+ */

    @Test
    public void get_is_alive() {
        /* execute */
        boolean alive = as(ANONYMOUS).getIsAlive();

        /* test */
        assertTrue(alive);
    }

}
