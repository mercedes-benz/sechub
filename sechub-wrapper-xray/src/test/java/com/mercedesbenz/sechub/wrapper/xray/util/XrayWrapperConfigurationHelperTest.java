// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperScanTypes;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperConfiguration;

class XrayWrapperConfigurationHelperTest {

    XrayWrapperConfigurationHelper helperToTest;

    @BeforeEach
    void beforeEach() {
        helperToTest = new XrayWrapperConfigurationHelper();
    }

    @Test
    void createXrayConfiguration_with_valid_parameters() throws XrayWrapperException {
        /* prepare */
        XrayWrapperConfiguration xrayWrapperConfiguration;
        // for valid parameters the artifactory and the registry can not be null or
        // empty
        try (MockedConstruction<EnvironmentVariableReader> mocked = mockConstruction(EnvironmentVariableReader.class, (mock, context) -> {
            when(mock.readEnvAsString(EnvironmentVariableConstants.ARTIFACTORY_ENV)).thenReturn("artifactoryMock");
            when(mock.readEnvAsString(EnvironmentVariableConstants.DOCKER_REGISTRY_ENV)).thenReturn("registerMock");

        })) {
            /* execute */
            xrayWrapperConfiguration = helperToTest.createXrayConfiguration(XrayWrapperScanTypes.DOCKER, "output", "workspace");

            /* test */
            assertEquals("output", xrayWrapperConfiguration.getXrayPdsReport());
            assertEquals("https://artifactoryMock", xrayWrapperConfiguration.getArtifactory());
            assertEquals("registerMock", xrayWrapperConfiguration.getRegistry());
        }
    }

    @Test
    void createXrayConfiguration_with_empty_artifactory_throws_xrayWrapperException() {
        /* prepare */
        try (MockedConstruction<EnvironmentVariableReader> mocked = mockConstruction(EnvironmentVariableReader.class, (mock, context) -> {
            when(mock.readEnvAsString(EnvironmentVariableConstants.ARTIFACTORY_ENV)).thenReturn("");
            when(mock.readEnvAsString(EnvironmentVariableConstants.DOCKER_REGISTRY_ENV)).thenReturn("registerMock");
        })) {
            /* execute */
            XrayWrapperException exception = assertThrows(XrayWrapperException.class,
                    () -> helperToTest.createXrayConfiguration(XrayWrapperScanTypes.DOCKER, "some", ""));

            /* test */
            assertEquals("Artifactory variable cannot be null or empty!", exception.getMessage());
        }
    }

    @Test
    void createXrayConfiguration_with_empty_registry_throws_xrayWrapperException() {
        /* prepare */
        try (MockedConstruction<EnvironmentVariableReader> mocked = mockConstruction(EnvironmentVariableReader.class, (mock, context) -> {
            when(mock.readEnvAsString(EnvironmentVariableConstants.ARTIFACTORY_ENV)).thenReturn("url");
            when(mock.readEnvAsString(EnvironmentVariableConstants.DOCKER_REGISTRY_ENV)).thenReturn(null);
        })) {
            /* execute */
            XrayWrapperException exception = assertThrows(XrayWrapperException.class,
                    () -> helperToTest.createXrayConfiguration(XrayWrapperScanTypes.DOCKER, "some", ""));

            /* test */
            assertEquals("Registry variable cannot be null or empty!", exception.getMessage());
        }
    }
}
