package com.mercedesbenz.sechub.xraywrapper.util;

import static com.mercedesbenz.sechub.xraywrapper.util.XrayWrapperConfigurationHelper.createXrayConfiguration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperScanTypes;
import com.mercedesbenz.sechub.xraywrapper.config.XrayWrapperConfiguration;

class XrayWrapperConfigurationHelperTest {

    @Test
    void test_createXrayConfiguration() {
        /* prepare */
        XrayWrapperConfiguration xrayWrapperConfiguration;

        /* execute + test */
        try (MockedConstruction<EnvironmentVariableReader> mocked = mockConstruction(EnvironmentVariableReader.class, (mock, context) -> {
            when(mock.readEnvAsString(EnvironmentVariableConstants.ARTIFACTORY_ENV)).thenReturn("artifactoryMock");
            when(mock.readEnvAsString(EnvironmentVariableConstants.DOCKER_REGISTRY_ENV)).thenReturn("registerMock");

        })) {
            xrayWrapperConfiguration = createXrayConfiguration(XrayWrapperScanTypes.DOCKER, "output");
            assertEquals("output", xrayWrapperConfiguration.getSecHubReport());
            assertEquals("https://artifactoryMock", xrayWrapperConfiguration.getArtifactory());
            assertEquals("registerMock", xrayWrapperConfiguration.getRegister());
        }
    }

    @Test
    void test_createXrayConfiguration_null() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> createXrayConfiguration(null, null));
    }
}
