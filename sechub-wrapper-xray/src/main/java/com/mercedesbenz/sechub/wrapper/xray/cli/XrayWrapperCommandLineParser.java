package com.mercedesbenz.sechub.wrapper.xray.cli;

import java.util.Arrays;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class XrayWrapperCommandLineParser {

    private JCommander commander;

    public record Arguments(String name, String checksum, XrayWrapperScanTypes scanType, String tag, String outputFile) {
    }

    public Arguments parseCommandLineArgs(String[] args) throws XrayWrapperCommandLineParserException {
        XrayWrapperCommandLineArgs xrayArgs = buildArguments(args);
        if (xrayArgs.isHelpRequired() || xrayArgs.getName().isEmpty() || xrayArgs.getChecksum().isEmpty()) {
            commander.usage();
            throw new XrayWrapperCommandLineParserException("Required parameters were empty" + Arrays.toString(args));
        }

        String name = xrayArgs.getName();
        String tag = "";
        XrayWrapperScanTypes type = XrayWrapperScanTypes.fromString(xrayArgs.getScanType());
        if (type.equals(XrayWrapperScanTypes.DOCKER)) {
            String[] image = splitContainerImage(xrayArgs.getName());
            if (image != null) {
                name = image[0];
                tag = image[1];
            }
        }

        String checksum = extractChecksum(xrayArgs.getChecksum());

        return new Arguments(name, checksum, type, tag, xrayArgs.getOutputFile());
    }

    private XrayWrapperCommandLineArgs buildArguments(String[] args) throws XrayWrapperCommandLineParserException {
        XrayWrapperCommandLineArgs xrayWrapperCommandLineArgs = new XrayWrapperCommandLineArgs();
        try {
            commander = JCommander.newBuilder().addObject(xrayWrapperCommandLineArgs).acceptUnknownOptions(false).build();
            commander.parse(args);
        } catch (ParameterException e) {
            throw new XrayWrapperCommandLineParserException("Could not parse parameters:" + Arrays.toString(args), e);
        }
        return xrayWrapperCommandLineArgs;
    }

    private String[] splitContainerImage(String image) {
        String[] splitImage = image.split(":");
        if (splitImage.length == 1) {
            return new String[] { splitImage[0], "latest" };
        }
        if (splitImage.length > 2) {
            return null;
        }
        return splitImage;
    }

    private String extractChecksum(String checksum) {
        return checksum.split(":")[1];
    }
}
