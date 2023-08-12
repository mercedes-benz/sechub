// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SystemTestConfigurationBuilderTest {

    @Test
    void builder_returns_configuration() {
        /* execute */
        /* @formatter:off */
        SystemTestConfiguration config = SystemTestConfiguration.builder().

            addVariable("var1","value1").
            localSetup().
                addSolution("solution1").
                    waitForAvailable().
                endSolution().
            endLocalSetup().
            test("test1").

            runSecHubJob().
                project("not-default-but-special").
            endRunSecHub().

            endTest().
        build();
        /* @formatter:on */

        /* test */
        assertNotNull(config);
    }

}
