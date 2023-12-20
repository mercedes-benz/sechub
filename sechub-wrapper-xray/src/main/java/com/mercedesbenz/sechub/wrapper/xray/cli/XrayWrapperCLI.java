// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperArtifact;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperConfiguration;
import com.mercedesbenz.sechub.wrapper.xray.util.XrayWrapperConfigurationHelper;

public class XrayWrapperCLI {

    private static final Logger LOG = LoggerFactory.getLogger(XrayWrapperCLI.class);

    public static void main(String[] args) {
        new XrayWrapperCLI().start(args);
    }

    void start(String[] args) {
        XrayWrapperCommandLineParser parser = new XrayWrapperCommandLineParser();
        final XrayWrapperCommandLineParser.Arguments arguments;

        try {
            arguments = parser.parseCommandLineArgs(args);
            XrayWrapperConfigurationHelper xrayWrapperConfigurationHelper = new XrayWrapperConfigurationHelper();
            XrayWrapperConfiguration xrayWrapperConfiguration = xrayWrapperConfigurationHelper.createXrayConfiguration(arguments.scanType(),
                    arguments.outputFile(), arguments.workspace());
            XrayWrapperArtifact artifact = new XrayWrapperArtifact(arguments.name(), arguments.checksum(), arguments.tag(), arguments.scanType());
            XrayWrapperArtifactoryClientSupport xrayWrapperArtifactoryClientSupport = new XrayWrapperArtifactoryClientSupport(xrayWrapperConfiguration,
                    artifact);

            // main program flow
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
