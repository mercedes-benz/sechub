package com.mercedesbenz.sechub.wrapper.xray.cli;

import static com.mercedesbenz.sechub.wrapper.xray.util.XrayWrapperConfigurationHelper.createXrayConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperArtifact;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperConfiguration;
import com.mercedesbenz.sechub.wrapper.xray.report.XrayWrapperReportException;

public class XrayWrapperCLI {

    private static final Logger LOG = LoggerFactory.getLogger(XrayWrapperCLI.class);

    public static void main(String[] args) {
        new XrayWrapperCLI().start(args);
    }

    XrayWrapperArtifactoryClientController xrayWrapperArtifactoryClientController;

    void start(String[] args) {
        XrayWrapperCommandLineParser parser = new XrayWrapperCommandLineParser();
        final XrayWrapperCommandLineParser.Arguments arguments;

        try {
            arguments = parser.parseCommandLineArgs(args);
            XrayWrapperConfiguration xrayWrapperConfiguration = createXrayConfiguration(arguments.scanType(), arguments.outputFile());
            XrayWrapperArtifact artifact = new XrayWrapperArtifact(arguments.name(), arguments.checksum(), arguments.tag(), arguments.scanType());
            xrayWrapperArtifactoryClientController = new XrayWrapperArtifactoryClientController(xrayWrapperConfiguration, artifact);

            // execute controller processing main program flow
            xrayWrapperArtifactoryClientController.waitForScansToFinishAndDownloadReport();

        } catch (XrayWrapperRuntimeException e) {
            LOG.error("An error occurred during the scan process: {}", e.getMessage(), e);
            System.exit(e.getExitCode().getExitCode());

        } catch (XrayWrapperCommandLineParserException e) {
            LOG.error("An error occurred while parsing the command line arguments: {}", e.getMessage(), e);
            System.exit(XrayWrapperExitCode.UNKNOWN_PARAMETERS.getExitCode());

        } catch (XrayWrapperReportException e) {
            LOG.error("An error occurred during report generation: {}", e.getMessage(), e);
            System.exit(e.getExitCode().getExitCode());
        }
    }
}
