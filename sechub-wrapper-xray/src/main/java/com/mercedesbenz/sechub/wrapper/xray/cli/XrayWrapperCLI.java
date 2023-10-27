package com.mercedesbenz.sechub.wrapper.xray.cli;

import static com.mercedesbenz.sechub.wrapper.xray.util.XrayWrapperConfigurationHelper.createXrayConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperArtifact;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperConfiguration;

public class XrayWrapperCLI {

    private static final Logger LOG = LoggerFactory.getLogger(XrayWrapperCLI.class);

    public static void main(String[] args) {
        new XrayWrapperCLI().start(args);
    }

    XrayWrapperArtifactoryClientSupport xrayWrapperArtifactoryClientSupport;

    void start(String[] args) {
        XrayWrapperCommandLineParser parser = new XrayWrapperCommandLineParser();
        final XrayWrapperCommandLineParser.Arguments arguments;

        try {
            arguments = parser.parseCommandLineArgs(args);
            XrayWrapperConfiguration xrayWrapperConfiguration = createXrayConfiguration(arguments.scanType(), arguments.outputFile());
            XrayWrapperArtifact artifact = new XrayWrapperArtifact(arguments.name(), arguments.checksum(), arguments.tag(), arguments.scanType());
            xrayWrapperArtifactoryClientSupport = new XrayWrapperArtifactoryClientSupport(xrayWrapperConfiguration, artifact);

            // execute controller processing main program flow
            xrayWrapperArtifactoryClientSupport.waitForScansToFinishAndDownloadReport();

        } catch (XrayWrapperException e) {
            LOG.error("An error occurred during the Wrapper execution: {}", e.getMessage(), e);
            System.exit(e.getExitCode().getExitCode());

        } catch (XrayWrapperCommandLineParserException e) {
            LOG.error("An error occurred while parsing the command line arguments: {}", e.getMessage(), e);
            System.exit(XrayWrapperExitCode.UNKNOWN_PARAMETERS.getExitCode());
        }
    }
}
