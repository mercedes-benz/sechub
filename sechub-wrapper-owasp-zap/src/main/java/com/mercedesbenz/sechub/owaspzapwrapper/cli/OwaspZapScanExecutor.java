// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ClientApi;

import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapClientApiFactory;
import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanContext;
import com.mercedesbenz.sechub.owaspzapwrapper.config.ProxyInformation;
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
        if (!connectionChecker.isTargetReachable(scanContext.getTargetUri(), scanContext.getProxyInformation())) {
            // Build error message containing proxy if it was set.
            String errorMessage = createErrorMessage(scanContext);
            throw new ZapWrapperRuntimeException(errorMessage, ZapWrapperExitCode.TARGET_URL_NOT_REACHABLE);
        }
        ClientApi clientApi = clientApiFactory.create(scanContext.getServerConfig());

        OwaspZapScan owaspZapScan = resolver.resolveScanImplementation(scanContext, clientApi);
        LOG.info("Starting Owasp Zap scan.");
        owaspZapScan.scan();

    }

    private String createErrorMessage(OwaspZapScanContext scanContext) {
        ProxyInformation proxyInformation = scanContext.getProxyInformation();

        String errorMessage = "Target url: " + scanContext.getTargetUri() + " is not reachable!";
        if (proxyInformation != null) {
            errorMessage += errorMessage + " via " + proxyInformation.getHost() + ":" + proxyInformation.getPort();
        }

        return errorMessage;
    }

}
