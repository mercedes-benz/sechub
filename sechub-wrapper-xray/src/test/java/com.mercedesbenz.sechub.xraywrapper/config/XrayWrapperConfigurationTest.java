package com.mercedesbenz.sechub.xraywrapper.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class XrayWrapperConfigurationTest {

    @Test
    public void test_XrayWrapperConfiguration() {
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
    public void test_XrayWrapperConfiguration_null() {
        /* execute + test */
        XrayWrapperConfiguration configuration = XrayWrapperConfiguration.Builder.create(null, null, null, null).build();
    }

}