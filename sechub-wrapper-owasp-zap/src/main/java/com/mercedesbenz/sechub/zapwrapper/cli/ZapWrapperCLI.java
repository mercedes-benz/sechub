// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.cli;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperCommandLineParser.ZapWrapperCommandLineParserException;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContext;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContextFactory;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScannerFactory;
import com.mercedesbenz.sechub.zapwrapper.config.ZapWrapperContextCreationException;
import com.mercedesbenz.sechub.zapwrapper.util.TargetConnectionChecker;

public class ZapWrapperCLI {
    private static final Logger LOG = LoggerFactory.getLogger(ZapWrapperCLI.class);

    public static void main(String[] args) throws IOException {
        new ZapWrapperCLI().start(args);
    }

    private void start(String[] args) throws IOException {
        ZapScanContext scanContext = null;
        try {
            LOG.info("Parsing command line parameters.");
            CommandLineSettings cmdSettings = parseCommandLineArguments(args);
            if (cmdSettings.isHelpRequired()) {
                System.exit(0);
            }
            LOG.info("Building the ZAP scan context.");
            scanContext = createZapScanContext(cmdSettings);
            LOG.info("Starting the ZAP scan.");
            startExecution(scanContext);
        } catch (ZapWrapperCommandLineParserException e) {
            LOG.error("An error occurred while parsing the command line arguments: {}", e.getMessage(), e);
            System.exit(ZapWrapperExitCode.UNSUPPORTED_COMMANDLINE_CONFIGURATION.getExitCode());
        } catch (ZapWrapperContextCreationException e) {
            LOG.error("An error occurred while creating ZAP scan context: {}", e.getMessage(), e);
            System.exit(e.getZapWrapperExitCode().getExitCode());
        } catch (ZapWrapperRuntimeException e) {
            LOG.error("An error occurred during the scan: {}.", e.getMessage(), e);
            if (scanContext == null) {
                LOG.warn("Scan context is null, cannot write product error as message!");
            } else {
                scanContext.getZapProductMessageHelper().writeProductError(e);
            }
            System.exit(e.getExitCode().getExitCode());
        }
    }

    private CommandLineSettings parseCommandLineArguments(String[] args) throws ZapWrapperCommandLineParserException {
        ZapWrapperCommandLineParser parser = new ZapWrapperCommandLineParser();
        return parser.parse(args);
    }

    private ZapScanContext createZapScanContext(CommandLineSettings cmdSettings) throws ZapWrapperContextCreationException {
        ZapScanContext scanContext;
        ZapScanContextFactory contextFactory = new ZapScanContextFactory();
        scanContext = contextFactory.create(cmdSettings);
        return scanContext;
    }

    private void startExecution(ZapScanContext scanContext) {
        ZapScanExecutor scanExecutor = new ZapScanExecutor(new ZapScannerFactory(), new TargetConnectionChecker());
        scanExecutor.execute(scanContext);
    }
}