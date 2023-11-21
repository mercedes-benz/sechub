package com.mercedesbenz.sechub.wrapper.xray.util;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;
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
    public static XrayWrapperConfiguration createXrayConfiguration(XrayWrapperScanTypes scanType, String outputFile, String workspace)
            throws XrayWrapperException {
        EnvironmentVariableReader environmentVariableReader = new EnvironmentVariableReader();
        String artifactoryUrl = "https://" + environmentVariableReader.readEnvAsString(EnvironmentVariableConstants.ARTIFACTORY_ENV);
        String zipDirectory = workspace + "/XrayArtifactoryReports";

        // get repository according to scan type
        String registry = null;
        if (scanType.equals(XrayWrapperScanTypes.DOCKER)) {
            registry = environmentVariableReader.readEnvAsString(EnvironmentVariableConstants.DOCKER_REGISTRY_ENV);
        }
        if (registry == null) {
            throw new XrayWrapperException("Registry variable cannot be null!", XrayWrapperExitCode.UNKNOWN_PARAMETERS);
        }
        return XrayWrapperConfiguration.Builder.builder().artifactory(artifactoryUrl).registry(registry).xrayPdsReport(outputFile).zipDirectory(zipDirectory)
                .build();
    }
}
