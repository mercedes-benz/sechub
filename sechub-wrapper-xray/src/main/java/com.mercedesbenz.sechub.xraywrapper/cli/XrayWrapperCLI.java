package com.mercedesbenz.sechub.xraywrapper.cli;

import static com.mercedesbenz.sechub.xraywrapper.util.XrayConfigurationCreater.createXrayConfiguration;

import java.io.IOException;

import com.mercedesbenz.sechub.xraywrapper.config.XrayConfiguration;
import com.mercedesbenz.sechub.xraywrapper.helper.XrayArtifact;

public class XrayWrapperCLI {

    public static void main(String[] args) {
        new XrayWrapperCLI().start(args);
    }

    private void start(String[] args) {
        XrayWrapperCommandLineParser parser = new XrayWrapperCommandLineParser();
        final XrayWrapperCommandLineParser.Arguments arguments = parser.parseCommandLineArgs(args);

        XrayConfiguration xrayConfiguration = createXrayConfiguration(arguments.scantype(), arguments.outputFile());
        XrayArtifact artifact = new XrayArtifact(arguments.name(), arguments.sha256(), arguments.tag(), arguments.scantype());

        XrayClientArtifactoryManager xrayClientArtifactoryManager = new XrayClientArtifactoryManager(xrayConfiguration, artifact);
        try {
            xrayClientArtifactoryManager.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
