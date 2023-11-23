package com.mercedesbenz.sechub.wrapper.xray.cli;

import com.beust.jcommander.Parameter;

public class XrayWrapperCommandLineArgs {
    @Parameter(names = { "--help", "-h" }, description = "Shows help and provides information on how to use the Xray wrapper.", help = true)
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
