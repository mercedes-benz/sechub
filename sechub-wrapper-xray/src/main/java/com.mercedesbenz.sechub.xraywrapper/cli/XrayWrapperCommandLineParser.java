package com.mercedesbenz.sechub.xraywrapper.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.mercedesbenz.sechub.xraywrapper.helper.XrayDockerImage;

public class XrayWrapperCommandLineParser {

    private JCommander commander;

    /**
     * Parse input arguments to create Xray docker image object
     *
     * @param args command line arguments
     * @return docker image saving name, tag and sha256
     */
    public XrayDockerImage parseDockerArguments(String[] args) {
        XrayCommandLineArgs xrayArgs = buildArguments(args);
        if (xrayArgs.isHelpRequired()) {
            commander.usage();
            return null;
        }

        if (xrayArgs.getImage().isEmpty() || xrayArgs.getSha256().isEmpty()) {
            commander.usage();
            return null;
        }

        String[] image = parseImage(xrayArgs.getImage());
        String sha256 = parseSha256(xrayArgs.getSha256());

        if (image != null)
            return new XrayDockerImage(image[0], image[1], sha256);

        return null;
    }

    /**
     * Parsing arguments with jcommander
     *
     * @param args command line arguments
     * @return parsed command line arguments
     */
    private XrayCommandLineArgs buildArguments(String[] args) {
        XrayCommandLineArgs xrayCommandLineArgs = new XrayCommandLineArgs();
        try {
            commander = JCommander.newBuilder().addObject(xrayCommandLineArgs).acceptUnknownOptions(false).build();
            commander.parse(args);
        } catch (ParameterException e) {
            // todo log error
            System.out.println("Error: unknown parameter");
        }
        return xrayCommandLineArgs;
    }

    private String[] parseImage(String image) {
        String[] s = image.split(":");
        if (s.length == 1)
            return new String[] { s[0], "latest" };
        if (s.length > 2)
            return null;
        return s;
    }

    private String parseSha256(String sha256) {
        return sha256.split(":")[1];
    }
}
