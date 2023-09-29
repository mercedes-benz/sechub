package com.mercedesbenz.sechub.xraywrapper.cli;

import static com.mercedesbenz.sechub.xraywrapper.util.XrayConfigurationBuilder.createXrayConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.xraywrapper.config.XrayArtifact;
import com.mercedesbenz.sechub.xraywrapper.config.XrayConfiguration;
import com.mercedesbenz.sechub.xraywrapper.reportgenerator.XrayWrapperReportException;

public class XrayWrapperCLI {

    private static final Logger LOG = LoggerFactory.getLogger(XrayWrapperCLI.class);

    public static void main(String[] args) {
        new XrayWrapperCLI().start(args);
    }

    XrayClientArtifactoryController xrayClientArtifactoryController;

    void start(String[] args) {
        XrayWrapperCommandLineParser parser = new XrayWrapperCommandLineParser();
        final XrayWrapperCommandLineParser.Arguments arguments;
        try {
            arguments = parser.parseCommandLineArgs(args);
            XrayConfiguration xrayConfiguration = createXrayConfiguration(arguments.scantype(), arguments.outputFile());
            XrayArtifact artifact = new XrayArtifact(arguments.name(), arguments.sha256(), arguments.tag(), arguments.scantype());
            xrayClientArtifactoryController = new XrayClientArtifactoryController(xrayConfiguration, artifact);
            xrayClientArtifactoryController.waitForScansToFinishAndDownloadReport();
        } catch (XrayWrapperRuntimeException e) {
            LOG.error("An error occurred during the scan process: {}", e.getMessage(), e);
            System.exit(e.getExitCode().getExitCode());

        } catch (XrayWrapperCommandLineParser.XrayWrapperCommandLineParserException e) {
            LOG.error("An error occurred while parsing the command line arguments: {}", e.getMessage(), e);
            System.exit(XrayWrapperExitCode.UNKNOWN_PARAMETERS.getExitCode());
        } catch (XrayWrapperReportException e) {
            LOG.error("An error occurred during report generation: {}", e.getMessage(), e);
            System.exit(e.getExitCode().getExitCode());
        }

    }
}
