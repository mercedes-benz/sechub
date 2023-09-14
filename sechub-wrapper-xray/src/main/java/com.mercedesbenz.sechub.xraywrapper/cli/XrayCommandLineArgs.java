package com.mercedesbenz.sechub.xraywrapper.cli;

import com.beust.jcommander.Parameter;

public class XrayCommandLineArgs {
    @Parameter(names = { "--help" }, description = "Shows help and provides information on how to use the Xray wrapper.", help = true)
    private boolean help;

    public boolean isHelpRequired() {
        return help;
    }

    @Parameter(names = "--image", description = "Name of the image you wish to scan example: my_image:1.0")
    private String image = "";

    public String getImage() {
        return image;
    }

    @Parameter(names = "--sha256", description = "Digest of the docker image you wish to scan example: sha256:xxx")
    private String sha256 = "";

    public String getSha256() {
        return sha256;
    }
}
