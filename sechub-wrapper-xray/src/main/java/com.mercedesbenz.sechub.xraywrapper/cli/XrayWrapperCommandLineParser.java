package com.mercedesbenz.sechub.xraywrapper.cli;

import java.util.Arrays;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class XrayWrapperCommandLineParser {

    private JCommander commander;

    public record Arguments(String name, String sha256, String scantype, String tag, String outputFile) {
    }

    public Arguments parseCommandLineArgs(String[] args) throws XrayWrapperCommandLineParserException {
        XrayCommandLineArgs xrayArgs = buildArguments(args);
        if (xrayArgs.isHelpRequired() || xrayArgs.getName().isEmpty() || xrayArgs.getSha256().isEmpty()) {
            commander.usage();
            throw new XrayWrapperCommandLineParserException("Error: required parameter was empty" + Arrays.toString(args));
        }

        String name = xrayArgs.getName();
        String tag = "";
        if (xrayArgs.getScantype().equals("docker")) {
            String[] image = parseImage(xrayArgs.getName());
            if (image != null) {
                name = image[0];
                tag = image[1];
            }
        }

        String sha256 = parseSha256(xrayArgs.getSha256());

        return new Arguments(name, sha256, xrayArgs.getScantype(), tag, xrayArgs.getOutputFile());
    }

    /**
     * Parsing arguments with jcommander
     *
     * @param args command line arguments
     * @return parsed command line arguments
     */
    private XrayCommandLineArgs buildArguments(String[] args) throws XrayWrapperCommandLineParserException {
        XrayCommandLineArgs xrayCommandLineArgs = new XrayCommandLineArgs();
        try {
            commander = JCommander.newBuilder().addObject(xrayCommandLineArgs).acceptUnknownOptions(false).build();
            commander.parse(args);
        } catch (ParameterException e) {
            throw new XrayWrapperCommandLineParserException("Error: could not parse parameters:" + Arrays.toString(args), e);
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
