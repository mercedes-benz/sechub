// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.owaspzapwrapper.cli.OwaspZapWrapperCommandLineParser.OwaspZapWrapperCommandLineParserException;
import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanConfiguration;

public class OwaspZapWrapperCLI {
    private static final Logger LOG = LoggerFactory.getLogger(OwaspZapWrapperCLI.class);

    public static void main(String[] args) {
        new OwaspZapWrapperCLI().start(args);
    }

    private void start(String[] args) {
        try {
            LOG.info("Building the scan configuration.");
            OwaspZapScanConfiguration scanConfig = resolveScanConfiguration(args);
            if (scanConfig == null) {
                /* only happens when help command was executed - here we just exit with 0 */
                System.exit(0);
            }
            LOG.info("Starting the scan.");
            startExecution(scanConfig);

        } catch (MustExitRuntimeException e) {
            LOG.error("Must exit with exit code {} because: {}.", e.getExitCode().getExitCode(), e.getMessage(), e);
            System.exit(e.getExitCode().getExitCode());
        }
    }

    private OwaspZapScanConfiguration resolveScanConfiguration(String[] args) {
        OwaspZapWrapperCommandLineParser parser = new OwaspZapWrapperCommandLineParser();

        OwaspZapScanConfiguration scanConfig = null;
        try {
            scanConfig = parser.parse(args);
        } catch (OwaspZapWrapperCommandLineParserException e) {
            LOG.error("An error occurred while parsing the command line arguments: ", e);
            throw new MustExitRuntimeException("Scan configuration was invalid.", e, MustExitCode.COMMANDLINE_CONFIGURATION_INVALID);
        }
        return scanConfig;
    }

    private void startExecution(OwaspZapScanConfiguration scanConfig) {
        OwaspZapScanExecutor scanExecutor = new OwaspZapScanExecutor();
        scanExecutor.execute(scanConfig);
    }
}