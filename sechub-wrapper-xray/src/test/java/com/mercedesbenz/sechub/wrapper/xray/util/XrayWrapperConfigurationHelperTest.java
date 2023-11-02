package com.mercedesbenz.sechub.wrapper.xray.util;

import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperScanTypes;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static com.mercedesbenz.sechub.wrapper.xray.util.XrayWrapperConfigurationHelper.createXrayConfiguration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class XrayWrapperConfigurationHelperTest {

    @Test
    void createXrayConfiguration_with_valid_parameters() {
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
            assertEquals("registerMock", xrayWrapperConfiguration.getRegistry());
        }
    }

    @Test
    void createXrayConfiguration_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> createXrayConfiguration(null, null));
    }
}
