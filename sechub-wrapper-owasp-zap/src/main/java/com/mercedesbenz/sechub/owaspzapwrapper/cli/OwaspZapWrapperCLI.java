// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.cli;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.owaspzapwrapper.cli.OwaspZapWrapperCommandLineParser.OwaspZapWrapperCommandLineParserException;
import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanContext;

public class OwaspZapWrapperCLI {
    private static final Logger LOG = LoggerFactory.getLogger(OwaspZapWrapperCLI.class);

    public static void main(String[] args) throws IOException {
        new OwaspZapWrapperCLI().start(args);
    }

    private void start(String[] args) throws IOException {
        OwaspZapScanContext scanContext = null;
        try {
            LOG.info("Building the scan configuration.");
            scanContext = resolveScanContext(args);
            if (scanContext == null) {
                /* only happens when help command was executed - here we just exit with 0 */
                System.exit(0);
            }
            LOG.info("Starting the scan.");
            startExecution(scanContext);

        } catch (ZapWrapperRuntimeException e) {
            LOG.error("An error occurred during the scan: {}.", e.getMessage(), e);
            scanContext.getOwaspZapProductMessagehelper().writeProductError(e);
            System.exit(e.getExitCode().getExitCode());

        } catch (OwaspZapWrapperCommandLineParserException e) {
            LOG.error("An error occurred while parsing the command line arguments: {}", e.getMessage(), e);
            System.exit(ZapWrapperExitCode.UNSUPPORTED_CONFIGURATION.getExitCode());
        }
    }

    private OwaspZapScanContext resolveScanContext(String[] args) throws OwaspZapWrapperCommandLineParserException {
        OwaspZapWrapperCommandLineParser parser = new OwaspZapWrapperCommandLineParser();
        return parser.parse(args);
    }

    private void startExecution(OwaspZapScanContext scanContext) {
        OwaspZapScanExecutor scanExecutor = new OwaspZapScanExecutor();
        scanExecutor.execute(scanContext);
    }
}