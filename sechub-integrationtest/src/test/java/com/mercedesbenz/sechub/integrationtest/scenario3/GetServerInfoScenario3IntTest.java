// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario3;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;

public class GetServerInfoScenario3IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario3.class);

    /* +-----------------------------------------------------------------------+ */
    /* +............................ Get server version .......................+ */
    /* +-----------------------------------------------------------------------+ */
    @Test
    public void get_server_version_normal_user_not_possible() {
        /* execute */
        expectHttpFailure(() -> as(Scenario3.USER_1).getServerVersion(), HttpStatus.FORBIDDEN);
    }

}
