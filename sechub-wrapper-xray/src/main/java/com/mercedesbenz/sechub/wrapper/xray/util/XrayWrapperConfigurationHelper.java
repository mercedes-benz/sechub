package com.mercedesbenz.sechub.wrapper.xray.util;

import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperScanTypes;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperConfiguration;

public class XrayWrapperConfigurationHelper {

    /**
     * builds a xray configuration from command line arguments and environment
     * variables
     *
     * @param scanType   scan typ for xray
     * @param outputFile output filename for xray report
     * @return xray scan configuration
     */
    public static XrayWrapperConfiguration createXrayConfiguration(XrayWrapperScanTypes scanType, String outputFile) {
        EnvironmentVariableReader environmentVariableReader = new EnvironmentVariableReader();
        String artifactoryUrl = "https://" + environmentVariableReader.readEnvAsString(EnvironmentVariableConstants.ARTIFACTORY_ENV);
        String zipDirectory = environmentVariableReader.readEnvAsString(EnvironmentVariableConstants.WORKSPACE_ENV) + "/XrayArtifactoryReports";

        // get repository according to scan type
        String repository = "";
        if (scanType.equals(XrayWrapperScanTypes.DOCKER)) {
            repository = environmentVariableReader.readEnvAsString(EnvironmentVariableConstants.DOCKER_REGISTRY_ENV);
        }
        return XrayWrapperConfiguration.Builder.builder(artifactoryUrl, repository, zipDirectory, outputFile).build();
    }
}
