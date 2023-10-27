package com.mercedesbenz.sechub.wrapper.xray.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class XrayWrapperConfigurationTest {

    @Test
    void xrayWrapperConfiguration_create_valid_configuration() {
        /* prepare */
        String artifactory = "myartifactory";
        String register = "register";
        String zipDir = "zipDir";
        String secHubReport = "sechubReport";

        /* execute */
        XrayWrapperConfiguration configuration = XrayWrapperConfiguration.Builder.builder(artifactory, register, zipDir, secHubReport).build();

        /* test */
        assertEquals(artifactory, configuration.getArtifactory());
    }

    @Test
    void xrayWrapperConfiguration_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> XrayWrapperConfiguration.Builder.builder(null, null, null, null).build());
    }

}