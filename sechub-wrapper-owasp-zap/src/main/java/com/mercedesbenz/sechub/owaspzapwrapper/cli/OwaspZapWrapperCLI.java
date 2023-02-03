// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.cli;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.owaspzapwrapper.cli.OwaspZapWrapperCommandLineParser.OwaspZapWrapperCommandLineParserException;
import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanContext;
import com.mercedesbenz.sechub.owaspzapwrapper.helper.OwaspZapProductMessageHelper;
import com.mercedesbenz.sechub.owaspzapwrapper.util.EnvironmentVariableConstants;
import com.mercedesbenz.sechub.owaspzapwrapper.util.EnvironmentVariableReader;

public class OwaspZapWrapperCLI {
    private static final Logger LOG = LoggerFactory.getLogger(OwaspZapWrapperCLI.class);

    private OwaspZapProductMessageHelper productMessagehelper;

    public static void main(String[] args) throws IOException {
        new OwaspZapWrapperCLI().start(args);
    }

    public OwaspZapWrapperCLI() {
        EnvironmentVariableReader reader = EnvironmentVariableReader.getInstance();
        String userMessagesFolder = reader.readAsString(EnvironmentVariableConstants.PDS_JOB_USER_MESSAGES_FOLDER);
        if (userMessagesFolder == null) {
            throw new IllegalStateException(
                    "PDS configuration invalid. Cannot send user messages, because environment variable PDS_JOB_USER_MESSAGES_FOLDER is not set.");
        }
        productMessagehelper = new OwaspZapProductMessageHelper(userMessagesFolder);
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
            productMessagehelper.writeProductMessages(scanContext.getProductMessages());

        } catch (ZapWrapperRuntimeException e) {
            if (ZapWrapperExitCode.SCAN_JOB_CANCELLED.equals(e.getExitCode())) {
                LOG.info(e.getMessage());
                productMessagehelper.writeProductMessages(scanContext.getProductMessages());
            } else {
                LOG.error("Must exit with exit code {} because: {}.", e.getExitCode().getExitCode(), e.getMessage(), e);
                productMessagehelper.writeProductError(e);
            }
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