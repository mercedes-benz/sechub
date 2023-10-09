package com.mercedesbenz.sechub.xraywrapper.util;

import static com.mercedesbenz.sechub.xraywrapper.util.XrayConfigurationBuilder.createXrayConfiguration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.mercedesbenz.sechub.xraywrapper.config.XrayConfiguration;

class XrayConfigurationBuilderTest {

    @Test
    public void test_createXrayConfiguration() {
        /* prepare */
        XrayConfiguration xrayConfiguration;

        /* execute + test */
        try (MockedConstruction<EnvironmentVariableReader> mocked = mockConstruction(EnvironmentVariableReader.class, (mock, context) -> {
            when(mock.readEnvAsString(EnvironmentVariableConstants.ARTIFACTORY_ENV)).thenReturn("artifactoryMock");
            when(mock.readEnvAsString(EnvironmentVariableConstants.REGISTER_ENV)).thenReturn("registerMock");

        })) {
            xrayConfiguration = createXrayConfiguration("docker", "output");
            assertEquals("docker", xrayConfiguration.getScan_type());
            assertEquals("output", xrayConfiguration.getSecHubReport());
            assertEquals("https://artifactoryMock", xrayConfiguration.getArtifactory());
            assertEquals("registerMock", xrayConfiguration.getRegister());
        }
    }

    @Test
    public void test_createXrayConfiguration_null() {
        /* execute + test */
        createXrayConfiguration(null, null);
    }
}
