// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.cli;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperCommandLineParser.ZapWrapperCommandLineParserException;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContext;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScannerFactory;
import com.mercedesbenz.sechub.zapwrapper.util.TargetConnectionChecker;

public class ZapWrapperCLI {
    private static final Logger LOG = LoggerFactory.getLogger(ZapWrapperCLI.class);

    public static void main(String[] args) throws IOException {
        new ZapWrapperCLI().start(args);
    }

    private void start(String[] args) throws IOException {
        ZapScanContext scanContext = null;
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
            scanContext.getZapProductMessageHelper().writeProductError(e);
            System.exit(e.getExitCode().getExitCode());

        } catch (ZapWrapperCommandLineParserException e) {
            LOG.error("An error occurred while parsing the command line arguments: {}", e.getMessage(), e);
            System.exit(ZapWrapperExitCode.UNSUPPORTED_CONFIGURATION.getExitCode());
        }
    }

    private ZapScanContext resolveScanContext(String[] args) throws ZapWrapperCommandLineParserException {
        ZapWrapperCommandLineParser parser = new ZapWrapperCommandLineParser();
        return parser.parse(args);
    }

    private void startExecution(ZapScanContext scanContext) {
        ZapScanExecutor scanExecutor = new ZapScanExecutor(new ZapScannerFactory(), new TargetConnectionChecker());
        scanExecutor.execute(scanContext);
    }
}