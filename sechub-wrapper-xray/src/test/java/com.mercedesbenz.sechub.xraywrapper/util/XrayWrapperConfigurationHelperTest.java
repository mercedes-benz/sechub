package com.mercedesbenz.sechub.xraywrapper.util;

import static com.mercedesbenz.sechub.xraywrapper.util.XrayWrapperConfigurationHelper.createXrayConfiguration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.mercedesbenz.sechub.xraywrapper.config.XrayWrapperConfiguration;

class XrayWrapperConfigurationHelperTest {

    @Test
    public void test_createXrayConfiguration() {
        /* prepare */
        XrayWrapperConfiguration xrayWrapperConfiguration;

        /* execute + test */
        try (MockedConstruction<EnvironmentVariableReader> mocked = mockConstruction(EnvironmentVariableReader.class, (mock, context) -> {
            when(mock.readEnvAsString(EnvironmentVariableConstants.ARTIFACTORY_ENV)).thenReturn("artifactoryMock");
            when(mock.readEnvAsString(EnvironmentVariableConstants.REGISTER_ENV)).thenReturn("registerMock");

        })) {
            xrayWrapperConfiguration = createXrayConfiguration("docker", "output");
            assertEquals("docker", xrayWrapperConfiguration.getScan_type());
            assertEquals("output", xrayWrapperConfiguration.getSecHubReport());
            assertEquals("https://artifactoryMock", xrayWrapperConfiguration.getArtifactory());
            assertEquals("registerMock", xrayWrapperConfiguration.getRegister());
        }
    }

    @Test
    public void test_createXrayConfiguration_null() {
        /* execute + test */
        createXrayConfiguration(null, null);
    }
}
