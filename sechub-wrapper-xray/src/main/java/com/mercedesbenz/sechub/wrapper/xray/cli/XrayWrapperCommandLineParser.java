// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.cli;

import java.util.Arrays;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;

public class XrayWrapperCommandLineParser {

    private JCommander commander;

    public record Arguments(String name, String checksum, XrayWrapperScanTypes scanType, String tag, String outputFile, String workspace) {
    }

    private record DockerImage(String name, String tag) {
    }

    public Arguments parseCommandLineArgs(String[] args) throws XrayWrapperException {
        XrayWrapperCommandLineArgs xrayArgs = buildArguments(args);
        if (xrayArgs.isHelpRequired()) {
            commander.usage();
            throw new XrayWrapperCommandLineParserException("Required parameters were empty: " + Arrays.toString(args));
        }
        if (xrayArgs.getName() == null || xrayArgs.getChecksum() == null || xrayArgs.getScanType() == null) {
            commander.usage();
            throw new XrayWrapperCommandLineParserException("Required parameters were empty: " + Arrays.toString(args));
        }

        // default artifact name and tag
        String name = xrayArgs.getName();
        String tag = "";

        XrayWrapperScanTypes type = XrayWrapperScanTypes.fromString(xrayArgs.getScanType());
        if (type.equals(XrayWrapperScanTypes.DOCKER)) {
            DockerImage dockerImage = splitContainerImageName(xrayArgs.getName());
            name = dockerImage.name();
            tag = dockerImage.tag();
        }

        String checksum = extractChecksum(xrayArgs.getChecksum());
        if (!checksum.matches("^[a-fA-F0-9]{64}$")) {
            throw new XrayWrapperCommandLineParserException("Checksum is not valid: " + checksum);
        }

        return new Arguments(name, checksum, type, tag, xrayArgs.getOutputFile(), xrayArgs.getWorkspace());
    }

    private XrayWrapperCommandLineArgs buildArguments(String[] args) {
        XrayWrapperCommandLineArgs xrayWrapperCommandLineArgs = new XrayWrapperCommandLineArgs();
        try {
            commander = JCommander.newBuilder().addObject(xrayWrapperCommandLineArgs).acceptUnknownOptions(false).build();
            commander.parse(args);
        } catch (ParameterException e) {
            throw new XrayWrapperCommandLineParserException("Could not parse parameters:" + Arrays.toString(args), e);
        }
        return xrayWrapperCommandLineArgs;
    }

    private DockerImage splitContainerImageName(String imageName) {
        if (imageName == null) {
            throw new XrayWrapperCommandLineParserException("Docker image name can not be null");
        }
        String[] splitImage = imageName.split(":");
        if (splitImage.length == 1) {
            return new DockerImage(splitImage[0], "latest");
        }
        if (splitImage.length > 2) {
            throw new XrayWrapperCommandLineParserException("Docker Image Name is invalid");
        }
        return new DockerImage(splitImage[0], splitImage[1]);
    }

    private String extractChecksum(String checksum) {
        if (checksum == null) {
            throw new XrayWrapperCommandLineParserException("Checksum can not be null");
        }
        try {
            return checksum.split(":")[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new XrayWrapperCommandLineParserException("Invalid Checksum", e);
        }
    }
}
