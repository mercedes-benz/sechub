package com.mercedesbenz.sechub.xraywrapper.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class XrayWrapperConfigurationTest {

    @Test
    void test_XrayWrapperConfiguration() {
        /* prepare */
        String artifactory = "myartifactory";
        String register = "register";
        String zipDir = "zipDir";
        String secHubReport = "sechubReport";

        /* execute */
        XrayWrapperConfiguration configuration = XrayWrapperConfiguration.Builder.create(artifactory, register, zipDir, secHubReport).build();

        /* test */
        assertEquals(artifactory, configuration.getArtifactory());
    }

    @Test
    void test_XrayWrapperConfiguration_null() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> XrayWrapperConfiguration.Builder.create(null, null, null, null).build());
    }

}