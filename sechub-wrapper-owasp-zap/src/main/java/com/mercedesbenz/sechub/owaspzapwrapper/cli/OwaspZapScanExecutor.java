// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ClientApi;

import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapClientApiFactory;
import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanConfiguration;
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

    public void execute(OwaspZapScanConfiguration scanConfig) throws ZapWrapperRuntimeException {
        if (!connectionChecker.isTargetReachable(scanConfig.getTargetUri(), scanConfig.getProxyInformation())) {
            // Build error message containing proxy if it was set.
            String errorMessage = createErrorMessage(scanConfig);
            throw new ZapWrapperRuntimeException(errorMessage, ZapWrapperExitCode.EXECUTION_FAILED);
        }
        ClientApi clientApi = null;

        clientApi = clientApiFactory.create(scanConfig.getServerConfig());

        OwaspZapScan owaspZapScan = resolver.resolveScanImplementation(scanConfig, clientApi);
        LOG.info("Starting Owasp Zap scan.");
        owaspZapScan.scan();

    }

    private String createErrorMessage(OwaspZapScanConfiguration scanConfig) {
        ProxyInformation proxyInformation = scanConfig.getProxyInformation();

        String errorMessage = "Target url: " + scanConfig.getTargetUri() + " is not reachable!";
        if (proxyInformation != null) {
            errorMessage += errorMessage + " via " + proxyInformation.getHost() + ":" + proxyInformation.getPort();
        }

        return errorMessage;
    }

}
