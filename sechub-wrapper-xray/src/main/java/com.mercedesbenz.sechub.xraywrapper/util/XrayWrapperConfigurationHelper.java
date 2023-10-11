package com.mercedesbenz.sechub.xraywrapper.util;

import com.mercedesbenz.sechub.xraywrapper.config.XrayWrapperConfiguration;

public class XrayWrapperConfigurationHelper {

    /**
     * builds a xray configuration from command line arguments and environment
     * variables
     *
     * @param scanType   scan typ for xray
     * @param outputFile output filename for xray report
     * @return xray scan configuration
     */
    public static XrayWrapperConfiguration createXrayConfiguration(String scanType, String outputFile) {
        EnvironmentVariableReader environmentVariableReader = new EnvironmentVariableReader();
        String artifactoryUrl = "https://" + environmentVariableReader.readEnvAsString(EnvironmentVariableConstants.ARTIFACTORY_ENV);
        String repository = environmentVariableReader.readEnvAsString(EnvironmentVariableConstants.REGISTER_ENV);
        String zipDirectory = environmentVariableReader.readEnvAsString(EnvironmentVariableConstants.WORKSPACE) + "/XrayArtifactoryReports";
        return XrayWrapperConfiguration.Builder.create(artifactoryUrl, repository, scanType, zipDirectory, outputFile).build();
    }
}
