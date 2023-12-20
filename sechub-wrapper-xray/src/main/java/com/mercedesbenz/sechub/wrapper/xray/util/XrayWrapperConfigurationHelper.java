// SPDX-License-Identifier: MIT
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
    public XrayWrapperConfiguration createXrayConfiguration(XrayWrapperScanTypes scanType, String outputFile, String workspace) throws XrayWrapperException {
        EnvironmentVariableReader environmentVariableReader = new EnvironmentVariableReader();

        // get artifactory url from ENV
        String artifactoryUrl = environmentVariableReader.readEnvAsString(EnvironmentVariableConstants.ARTIFACTORY_ENV);
        if (artifactoryUrl == null || artifactoryUrl.isEmpty()) {
            throw new XrayWrapperException("Artifactory variable cannot be null or empty!", XrayWrapperExitCode.UNKNOWN_PARAMETERS);
        }
        artifactoryUrl = "https://" + artifactoryUrl;

        String zipDirectory = workspace + "/XrayArtifactoryReports";

        // get registry according to scan type
        String registry = null;
        if (scanType.equals(XrayWrapperScanTypes.DOCKER)) {
            registry = environmentVariableReader.readEnvAsString(EnvironmentVariableConstants.DOCKER_REGISTRY_ENV);
        }
        if (registry == null || registry.isEmpty()) {
            throw new XrayWrapperException("Registry variable cannot be null or empty!", XrayWrapperExitCode.UNKNOWN_PARAMETERS);
        }
        return XrayWrapperConfiguration.Builder.builder().artifactory(artifactoryUrl).registry(registry).xrayPdsReport(outputFile).zipDirectory(zipDirectory)
                .build();
    }
}
