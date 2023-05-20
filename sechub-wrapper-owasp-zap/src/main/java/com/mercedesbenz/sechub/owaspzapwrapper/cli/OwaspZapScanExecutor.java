// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ClientApi;

import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapClientApiFactory;
import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanContext;
import com.mercedesbenz.sechub.owaspzapwrapper.scan.OwaspZapScan;
import com.mercedesbenz.sechub.owaspzapwrapper.util.TargetConnectionChecker;

public class OwaspZapScanExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(OwaspZapScanExecutor.class);

    OwaspZapScanResolver resolver;
    OwaspZapClientApiFactory clientApiFactory;

    TargetConnectionChecker connectionChecker;

    public OwaspZapScanExecutor() {
        clientApiFactory = new OwaspZapClientApiFactory();
        resolver = new OwaspZapScanResolver();
        connectionChecker = new TargetConnectionChecker();
    }

    public void execute(OwaspZapScanContext scanContext) throws ZapWrapperRuntimeException {
        if (scanContext.connectionCheckEnabled()) {
            connectionChecker.assertApplicationIsReachable(scanContext);
        }

        ClientApi clientApi = clientApiFactory.create(scanContext.getServerConfig());

        OwaspZapScan owaspZapScan = resolver.resolveScanImplementation(scanContext, clientApi);
        LOG.info("Starting Owasp Zap scan.");
        owaspZapScan.scan();
    }
}
