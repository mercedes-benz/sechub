// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ClientApi;

import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanContext;
import com.mercedesbenz.sechub.owaspzapwrapper.config.auth.AuthenticationType;
import com.mercedesbenz.sechub.owaspzapwrapper.scan.OwaspZapScan;
import com.mercedesbenz.sechub.owaspzapwrapper.scan.UnauthenticatedScan;
import com.mercedesbenz.sechub.owaspzapwrapper.scan.auth.HTTPBasicAuthScan;

public class OwaspZapScanResolver {
    private static final Logger LOG = LoggerFactory.getLogger(OwaspZapScanResolver.class);

    public OwaspZapScan resolveScanImplementation(OwaspZapScanContext scanContext, ClientApi clientApi) {
        LOG.info("Resolve scan implementation.");
        OwaspZapScan scan;
        AuthenticationType authenticationType = scanContext.getAuthenticationType();
        if (authenticationType == null) {
            throw new ZapWrapperRuntimeException("No matching scan type could be found.", ZapWrapperExitCode.UNSUPPORTED_CONFIGURATION);
        }

        switch (authenticationType) {
        case UNAUTHENTICATED:
            scan = new UnauthenticatedScan(clientApi, scanContext);
            LOG.info("Using unauthenticated scan");
            break;
        case HTTP_BASIC_AUTHENTICATION:
            scan = new HTTPBasicAuthScan(clientApi, scanContext);
            LOG.info("Using http basic authentication scan");
            break;
        default:
            throw new ZapWrapperRuntimeException("No matching scan type could be found.", ZapWrapperExitCode.UNSUPPORTED_CONFIGURATION);
        }
        return scan;
    }

}
