// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.cli;

import com.beust.jcommander.Parameter;

public class XrayWrapperCommandLineArgs {
    @Parameter(names = { "--help",
            "-h" }, description = """
                    The Xray wrapper communicates with an instance of a Jfrog artifactory with Xray installed. The wrapper can check the scan status, download required reports and delete artifacts. The Wrapper can transform additional information from the Security Report into the CycloneDX report.
                    \t\tPlease ensure that the following required environment variables are set:
                    \t\tXRAY_ARTIFACTORY=example-artifactory-url.com
                    \t\tXRAY_DOCKER_REGISTRY=example-artifactory-docker-registry-name
                    \t\tXRAY_USERNAME=user
                    \t\tXRAY_PASSWORD=password
                    """, help = true)
    private boolean help;

    @Parameter(names = { "--name", "-n" }, description = "Name of the image or artifact you wish to scan example: my_image:1.0")
    private String name;

    @Parameter(names = { "--checksum", "-cs" }, description = "SHA256 digest of the docker image you wish to scan example: sha256:8he98dhojw92hodnk")
    private String checksum;

    @Parameter(names = { "--scantype" }, description = "Scan type of the Xray scan, need to be one of the following: docker")
    private String scanType;

    @Parameter(names = { "--outputfile", "-o" }, description = "Name of the CycloneDX outputfile")
    private String outputFile = "";

    @Parameter(names = { "--workspace", "-w" }, description = "Workspace for Xray wrapper")
    private String workspace = "";

    public boolean isHelpRequired() {
        return help;
    }

    public String getName() {
        return name;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getScanType() {
        return scanType;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public String getWorkspace() {
        return workspace;
    }

}
