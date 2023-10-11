package com.mercedesbenz.sechub.xraywrapper.cli;

import com.beust.jcommander.Parameter;

public class XrayWrapperCommandLineArgs {
    @Parameter(names = { "--help", "-h" }, description = "Shows help and provides information on how to use the Xray wrapper.", help = true)
    private boolean help;

    public boolean isHelpRequired() {
        return help;
    }

    @Parameter(names = { "--name", "-n" }, description = "Name of the image or artifact you wish to scan example: my_image:1.0")
    private String name = "";

    public String getName() {
        return name;
    }

    @Parameter(names = { "--sha256", "-s" }, description = "Digest of the docker image you wish to scan example: sha256:xxx")
    private String sha256 = "";

    public String getSha256() {
        return sha256;
    }

    @Parameter(names = { "--scantype" }, description = "Scan type of the xray scan, need to be one of the following: docker")
    private String scanType = "";

    public String getScanType() {
        return scanType;
    }

    @Parameter(names = { "--outputfile", "-o" }, description = "Name of the outputfile")
    private String outputFile = "";

    public String getOutputFile() {
        return outputFile;
    }

}
