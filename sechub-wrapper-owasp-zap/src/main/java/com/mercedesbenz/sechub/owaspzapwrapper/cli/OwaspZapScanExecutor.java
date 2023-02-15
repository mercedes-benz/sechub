// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.cli;

import java.net.URL;
import java.util.Iterator;

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
        assertTargetReachable(scanContext);

        ClientApi clientApi = clientApiFactory.create(scanContext.getServerConfig());

        OwaspZapScan owaspZapScan = resolver.resolveScanImplementation(scanContext, clientApi);
        LOG.info("Starting Owasp Zap scan.");
        owaspZapScan.scan();
    }

    private void assertTargetReachable(OwaspZapScanContext scanContext) {
        boolean isReachable = false;
        Iterator<URL> iterator = scanContext.getOwaspZapURLsIncludeList().iterator();
        while (iterator.hasNext() && isReachable == false) {
            // trying to reach the target URL and all includes until the first reachable
            // URL is found.
            isReachable = isSiteCurrentlyReachable(scanContext, iterator.next(), scanContext.getMaxNumberOfConnectionRetries(),
                    scanContext.getRetryWaittimeInMilliseconds());
        }
        if (!isReachable) {
            // Build error message containing proxy if it was set.
            String errorMessage = createErrorMessage(scanContext);
            throw new ZapWrapperRuntimeException(errorMessage, ZapWrapperExitCode.TARGET_URL_NOT_REACHABLE);
        }
    }

    private boolean isSiteCurrentlyReachable(OwaspZapScanContext scanContext, URL url, int maxNumberOfConnectionRetries, int retryWaittimeInMilliseconds) {
        for (int i = 0; i < maxNumberOfConnectionRetries; i++) {
            // do not wait on first try
            if (i > 0) {
                wait(retryWaittimeInMilliseconds);
            }
            if (connectionChecker.isTargetReachable(url, scanContext.getProxyInformation())) {
                return true;
            }
        }
        return false;
    }

    private void wait(int waittimeInMilliseconds) {
        try {
            Thread.sleep(waittimeInMilliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String createErrorMessage(OwaspZapScanContext scanContext) {
        ProxyInformation proxyInformation = scanContext.getProxyInformation();

        String errorMessage = "Target url: " + scanContext.getTargetUrl() + " is not reachable";
        if (proxyInformation != null) {
            errorMessage += errorMessage + " via " + proxyInformation.getHost() + ":" + proxyInformation.getPort();
        }
        return errorMessage;
    }
}
