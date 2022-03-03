// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.owaspzapwrapper.cli.OwaspZapScanExecutor.OwaspZapScanExecutorException;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.OwaspZapWrapperCommandLineParser.OwaspZapWrapperCommandLineParserException;
import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanConfiguration;

public class OwaspZapWrapperCLI {
    private static final Logger LOG = LoggerFactory.getLogger(OwaspZapWrapperCLI.class);

    public static void main(String[] args) {
        OwaspZapWrapperCommandLineParser parser = new OwaspZapWrapperCommandLineParser();
        OwaspZapScanConfiguration scanConfig = null;
        try {
            scanConfig = parser.parse(args);
        } catch (OwaspZapWrapperCommandLineParserException e) {
            LOG.error("An error occurred while parsing the command line arguments: ", e.getCause());
            System.exit(1);
        }
        if (scanConfig == null) {
            /* can happen when help command was executed - just exit with 0 */
            System.exit(0);
        }

        OwaspZapScanExecutor scanExecutor = new OwaspZapScanExecutor();
        try {
            scanExecutor.execute(scanConfig);
        } catch (OwaspZapScanExecutorException e) {
            LOG.error("An error occurred while executing an Owasp Zap scan because: {}", e.getMessage());
            System.exit(2);
        }
    }
}