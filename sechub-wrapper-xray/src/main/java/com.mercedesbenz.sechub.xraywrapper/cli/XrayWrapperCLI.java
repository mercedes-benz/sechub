package com.mercedesbenz.sechub.xraywrapper.cli;

import java.io.IOException;

import com.mercedesbenz.sechub.xraywrapper.config.XrayConfiguration;
import com.mercedesbenz.sechub.xraywrapper.helper.XrayDockerImage;
import com.mercedesbenz.sechub.xraywrapper.util.EnvironmentVairiableReader;
import com.mercedesbenz.sechub.xraywrapper.util.EnvironmentVariableConstants;

public class XrayWrapperCLI {

    public static void main(String[] args) throws IOException {
        new XrayWrapperCLI().start(args);
    }

    private void start(String[] args) throws IOException {

        XrayConfiguration xrayConfiguration = getXrayConfiguration();

        // args should contain docker_image_name:tag sha256 of docker_image
        if (args.length < 2) {
            System.out.println("XrayWrapperCLI: no docker image and SHA56 have been passed");
            // exit(0);
        }

        // todo: make argument parser class
        String[] s = args[0].split(":");
        String[] sha = args[1].split(":");
        XrayDockerImage image = new XrayDockerImage(s[0], s[1], sha[1]);

        XrayClientArtifactoryManager xrayClientArtifactoryManager = new XrayClientArtifactoryManager(xrayConfiguration, image);
        xrayClientArtifactoryManager.start();
    }

    private static XrayConfiguration getXrayConfiguration() {
        EnvironmentVairiableReader environmentVairiableReader = new EnvironmentVairiableReader();

        String baseUrl = "https://" + environmentVairiableReader.readEnvAsString(EnvironmentVariableConstants.ARTIFACTORY_ENV);
        String repository = environmentVairiableReader.readEnvAsString(EnvironmentVariableConstants.REGISTER_ENV);
        String reportfiles = environmentVairiableReader.readEnvAsString(EnvironmentVariableConstants.WORKSPACE) + "/XrayArtifactoryReports";

        return new XrayConfiguration(baseUrl, repository, "docker", reportfiles);
    }
}
