// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapClientApiFactory;
import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanContext;
import com.mercedesbenz.sechub.owaspzapwrapper.scan.ClientApiFacade;
import com.mercedesbenz.sechub.owaspzapwrapper.scan.OwaspZapScanner;
import com.mercedesbenz.sechub.owaspzapwrapper.util.TargetConnectionChecker;

public class OwaspZapScanExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(OwaspZapScanExecutor.class);

    OwaspZapClientApiFactory clientApiFactory;

    TargetConnectionChecker connectionChecker;

    public OwaspZapScanExecutor() {
        clientApiFactory = new OwaspZapClientApiFactory();
        connectionChecker = new TargetConnectionChecker();
    }

    public void execute(OwaspZapScanContext scanContext) throws ZapWrapperRuntimeException {
        if (scanContext.connectionCheckEnabled()) {
            connectionChecker.assertApplicationIsReachable(scanContext);
        }

        ClientApiFacade clientApiFacade = clientApiFactory.create(scanContext.getServerConfig());

        OwaspZapScanner owaspZapScanner = new OwaspZapScanner(clientApiFacade, scanContext);
        LOG.info("Starting Owasp Zap scan.");
        owaspZapScanner.scan();
    }
}
