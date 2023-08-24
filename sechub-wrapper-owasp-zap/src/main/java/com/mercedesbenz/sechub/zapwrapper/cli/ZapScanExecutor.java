// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.zapwrapper.config.ZapClientApiFactory;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContext;
import com.mercedesbenz.sechub.zapwrapper.scan.ClientApiFacade;
import com.mercedesbenz.sechub.zapwrapper.scan.ZapScanner;
import com.mercedesbenz.sechub.zapwrapper.util.TargetConnectionChecker;

public class ZapScanExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(ZapScanExecutor.class);

    ZapClientApiFactory clientApiFactory;

    TargetConnectionChecker connectionChecker;

    public ZapScanExecutor() {
        clientApiFactory = new ZapClientApiFactory();
        connectionChecker = new TargetConnectionChecker();
    }

    public void execute(ZapScanContext scanContext) throws ZapWrapperRuntimeException {
        if (scanContext.connectionCheckEnabled()) {
            connectionChecker.assertApplicationIsReachable(scanContext);
        }

        ClientApiFacade clientApiFacade = clientApiFactory.create(scanContext.getServerConfig());

        ZapScanner zapScanner = new ZapScanner(clientApiFacade, scanContext);
        LOG.info("Starting Zap scan.");
        zapScanner.scan();
    }
}
