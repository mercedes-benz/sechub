package com.mercedesbenz.sechub.xraywrapper.cli;

import static com.mercedesbenz.sechub.xraywrapper.util.XrayConfigurationBuilder.createXrayConfiguration;

import java.io.IOException;

import com.mercedesbenz.sechub.xraywrapper.config.XrayArtifact;
import com.mercedesbenz.sechub.xraywrapper.config.XrayConfiguration;

public class XrayWrapperCLI {

    public static void main(String[] args) {
        new XrayWrapperCLI().start(args);
    }

    XrayClientArtifactoryController xrayClientArtifactoryController;

    void start(String[] args) {
        XrayWrapperCommandLineParser parser = new XrayWrapperCommandLineParser();
        final XrayWrapperCommandLineParser.Arguments arguments = parser.parseCommandLineArgs(args);

        XrayConfiguration xrayConfiguration = createXrayConfiguration(arguments.scantype(), arguments.outputFile());
        XrayArtifact artifact = new XrayArtifact(arguments.name(), arguments.sha256(), arguments.tag(), arguments.scantype());

        // xrayClientArtifactoryManager =
        // XrayClientArtifactoryManager.Builder.create(xrayConfiguration,
        // artifact).build();

        xrayClientArtifactoryController = new XrayClientArtifactoryController(xrayConfiguration, artifact);
        try {
            xrayClientArtifactoryController.waitForScansToFinishAndDownloadReport();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
