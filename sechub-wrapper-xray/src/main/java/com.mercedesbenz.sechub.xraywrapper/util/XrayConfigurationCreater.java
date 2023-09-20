package com.mercedesbenz.sechub.xraywrapper.util;

import com.mercedesbenz.sechub.xraywrapper.config.XrayConfiguration;

public class XrayConfigurationCreater {

    public static XrayConfiguration createXrayConfiguration(String scanType, String outputFile) {
        EnvironmentVariableReader environmentVariableReader = new EnvironmentVariableReader();
        String artifactoryUrl = "https://" + environmentVariableReader.readEnvAsString(EnvironmentVariableConstants.ARTIFACTORY_ENV);
        String repository = environmentVariableReader.readEnvAsString(EnvironmentVariableConstants.REGISTER_ENV);
        String zipDirectory = environmentVariableReader.readEnvAsString(EnvironmentVariableConstants.WORKSPACE) + "/XrayArtifactoryReports";

        return new XrayConfiguration(artifactoryUrl, repository, scanType, zipDirectory, outputFile);
    }
}
