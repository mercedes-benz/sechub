// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContext;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScannerFactory;
import com.mercedesbenz.sechub.zapwrapper.scan.ZapScanner;
import com.mercedesbenz.sechub.zapwrapper.util.TargetConnectionChecker;

public class ZapScanExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(ZapScanExecutor.class);

    ZapScannerFactory zapScannerFactory;

    TargetConnectionChecker connectionChecker;

    public ZapScanExecutor() {
        zapScannerFactory = new ZapScannerFactory();
        connectionChecker = new TargetConnectionChecker();
    }

    public void execute(ZapScanContext scanContext) throws ZapWrapperRuntimeException {
        if (scanContext.connectionCheckEnabled()) {
            connectionChecker.assertApplicationIsReachable(scanContext);
        }

        ZapScanner zapScanner = zapScannerFactory.create(scanContext);

        LOG.info("Starting Zap scan.");
        zapScanner.scan();
    }
}
