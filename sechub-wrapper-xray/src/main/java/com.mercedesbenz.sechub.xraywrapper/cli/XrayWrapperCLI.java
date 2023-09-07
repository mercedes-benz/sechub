package com.mercedesbenz.sechub.xraywrapper.cli;

import java.io.IOException;

import com.mercedesbenz.sechub.xraywrapper.helper.XrayDockerImage;

public class XrayWrapperCLI {

    public static void main(String[] args) throws IOException {
        new XrayWrapperCLI().start(args);
    }

    private void start(String[] args) throws IOException {
        String baseUrl = "https://artifacts.i.mercedes-benz.com";
        String repository = "sechubm-dev-docker-local";
        String reportfiles = "XrayReports";

        // args should contain docker_image_name:tag sha256 of docker_image
        if (args.length < 2) {
            System.out.println("XrayWrapperCLI: no docker image and SHA56 have been passed");
            // exit(0);
        }

        // todo: make argument parser class
        String[] s = args[0].split(":");
        String[] sha = args[1].split(":");
        XrayDockerImage image = new XrayDockerImage(s[0], s[1], sha[1]);

        XrayClientArtifactoryManager xrayClientArtifactoryManager = new XrayClientArtifactoryManager(baseUrl, repository, image, reportfiles);
        xrayClientArtifactoryManager.start();
    }
}
