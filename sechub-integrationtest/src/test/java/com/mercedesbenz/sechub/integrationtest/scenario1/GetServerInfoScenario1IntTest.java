// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario1;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;

public class GetServerInfoScenario1IntTest {

    private static final String VERSIONPATTERN_MAJOR_MINOR_HOTFIX = "[0-9]+\\.[0-9]+\\.[0-9]+.*";

    private static final Pattern PATTERN_ONLY_MAJOR_MINOR_HOTFIX = Pattern.compile(VERSIONPATTERN_MAJOR_MINOR_HOTFIX);

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario1.class);

    /* +-----------------------------------------------------------------------+ */
    /* +............................ Get server version .......................+ */
    /* +-----------------------------------------------------------------------+ */

    @Test
    public void get_server_version_anonymous_not_possible_results_in_unauthorized() {
        /* execute */
        expectHttpFailure(() -> as(ANONYMOUS).getServerVersion(), HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void get_server_version_as_admin_is_possible() {
        record ServerVersion(String serverVersion) {
        }
        ;

        /* execute */
        String json = as(SUPER_ADMIN).getServerVersion();

        ServerVersion serverVersion = JSONConverter.get().fromJSON(ServerVersion.class, json);

        /* test */
        /* version must be X.Y.Z and not something ala vX.Y.Z or X.Y.Z-server" */
        assertFalse(serverVersion.serverVersion.startsWith("v"));
        assertFalse(serverVersion.serverVersion.endsWith("-server"));
        /* check format is like regexp */
        assertTrue("Given version is not accepted:" + serverVersion.serverVersion + "\nExpected format like:" + VERSIONPATTERN_MAJOR_MINOR_HOTFIX,
                PATTERN_ONLY_MAJOR_MINOR_HOTFIX.matcher(serverVersion.serverVersion).matches());
    }

}
