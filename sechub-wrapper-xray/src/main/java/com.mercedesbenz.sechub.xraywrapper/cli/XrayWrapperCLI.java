package com.mercedesbenz.sechub.xraywrapper.cli;

import java.io.IOException;
import java.util.Objects;

import com.mercedesbenz.sechub.xraywrapper.config.XrayConfiguration;
import com.mercedesbenz.sechub.xraywrapper.helper.XrayDockerImage;
import com.mercedesbenz.sechub.xraywrapper.util.EnvironmentVariableConstants;
import com.mercedesbenz.sechub.xraywrapper.util.EnvironmentVariableReader;

public class XrayWrapperCLI {

    public static void main(String[] args) throws IOException {
        new XrayWrapperCLI().start(args);
    }

    private void start(String[] args) {
        XrayConfiguration xrayConfiguration = getXrayConfiguration();

        if (Objects.equals(xrayConfiguration.getScan_type(), "docker")) {
            startDockerScan(args, xrayConfiguration);
        }
    }

    /**
     * Get the configurations from the environment variables
     *
     * @return the configurations
     */
    private static XrayConfiguration getXrayConfiguration() {
        EnvironmentVariableReader environmentVairiableReader = new EnvironmentVariableReader();

        String baseUrl = "https://" + environmentVairiableReader.readEnvAsString(EnvironmentVariableConstants.ARTIFACTORY_ENV);
        String repository = environmentVairiableReader.readEnvAsString(EnvironmentVariableConstants.REGISTER_ENV);
        String zipDirectory = environmentVairiableReader.readEnvAsString(EnvironmentVariableConstants.WORKSPACE) + "/XrayArtifactoryReports";
        String pds_report_file = environmentVairiableReader.readEnvAsString(EnvironmentVariableConstants.PDS_JOB_RESULT_FILE);

        return new XrayConfiguration(baseUrl, repository, "docker", zipDirectory, pds_report_file);
    }

    /**
     * Starting a docker image scan
     *
     * @param args              command line argument
     * @param xrayConfiguration starts scan with the configuration
     */
    private void startDockerScan(String[] args, XrayConfiguration xrayConfiguration) {
        XrayWrapperCommandLineParser parser = new XrayWrapperCommandLineParser();
        XrayDockerImage image = parser.parseDockerArguments(args);
        if (image != null) {
            XrayClientArtifactoryManager xrayClientArtifactoryManager = new XrayClientArtifactoryManager(xrayConfiguration, image);
            try {
                xrayClientArtifactoryManager.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Error: could not parse argument input to docker image");
            System.exit(0);
        }
    }
}
