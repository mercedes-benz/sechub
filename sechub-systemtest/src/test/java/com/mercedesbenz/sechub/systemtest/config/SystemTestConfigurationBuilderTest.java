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
                    waitForAVailable().
                endSolution().
            endLocalSetup().
        build();
        /* @formatter:on */

        /* test */
        assertNotNull(config);
    }

}
