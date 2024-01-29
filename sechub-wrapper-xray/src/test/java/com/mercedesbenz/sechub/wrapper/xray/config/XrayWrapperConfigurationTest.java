// SPDX-License-Identifier: MIT
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
        String xrayPdsReport = "xrayPdsReport";

        /* execute */
        XrayWrapperConfiguration configuration = XrayWrapperConfiguration.Builder.builder().artifactory(artifactory).registry(register).zipDirectory(zipDir)
                .xrayPdsReport(xrayPdsReport).build();

        /* test */
        assertEquals(artifactory, configuration.getArtifactory());
        assertEquals(register, configuration.getRegistry());
        assertEquals(zipDir, configuration.getZipDirectory());
        assertEquals(xrayPdsReport, configuration.getXrayPdsReport());
    }

    @Test
    void xrayWrapperConfiguration_builder_without_parameters_throws_IllegalStateException() {
        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> XrayWrapperConfiguration.Builder.builder().build());

        /* test */
        assertEquals("Artifactory URL or Zip file directory cannot be null", exception.getMessage());
    }

}