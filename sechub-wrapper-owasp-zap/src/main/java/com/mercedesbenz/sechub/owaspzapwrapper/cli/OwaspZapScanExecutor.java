// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.cli;

import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapClientApiFactory;
import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanConfiguration;
import com.mercedesbenz.sechub.owaspzapwrapper.config.auth.AuthenticationType;
import com.mercedesbenz.sechub.owaspzapwrapper.scan.OwaspZapScan;
import com.mercedesbenz.sechub.owaspzapwrapper.scan.UnauthenticatedScan;
import com.mercedesbenz.sechub.owaspzapwrapper.scan.auth.HTTPBasicAuthScan;
import com.mercedesbenz.sechub.owaspzapwrapper.util.TargetConnectionChecker;

public class OwaspZapScanExecutor {

    public class OwaspZapScanExecutorException extends Exception {

        private static final long serialVersionUID = 1L;

        public OwaspZapScanExecutorException(String message) {
            super(message);
        }

    }

    public void execute(OwaspZapScanConfiguration scanConfig) throws OwaspZapScanExecutorException {
        if (!TargetConnectionChecker.isSiteReachable(scanConfig.getTargetUri(), scanConfig.getProxyInformation())) {
            throw new OwaspZapScanExecutorException("Target url: " + scanConfig.getTargetUri() + " is not reachable!");
        }
        ClientApi clientApi = null;

        try {
            clientApi = new OwaspZapClientApiFactory().create(scanConfig);
        } catch (ClientApiException e) {
            throw new OwaspZapScanExecutorException("Creating Owasp Zap ClientApi object failed because: " + e.getMessage());
        }

        OwaspZapScan owaspZapScan = resolveScanImplementation(scanConfig, clientApi);
        owaspZapScan.scan();

    }

    private OwaspZapScan resolveScanImplementation(OwaspZapScanConfiguration scanConfig, ClientApi clientApi) {
        OwaspZapScan scan;
        AuthenticationType authenticationType = scanConfig.getAuthenticationType();
        switch (authenticationType) {
        case UNAUTHENTICATED:
            scan = new UnauthenticatedScan(clientApi, scanConfig);
            break;
        case HTTP_BASIC_AUTHENTICATION:
            scan = new HTTPBasicAuthScan(clientApi, scanConfig);
            break;
        default:
            scan = new UnauthenticatedScan(clientApi, scanConfig);
            break;
        }
        return scan;
    }

}
