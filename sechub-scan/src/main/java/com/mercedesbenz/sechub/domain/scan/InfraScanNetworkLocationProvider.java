// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.net.InetAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.mercedesbenz.sechub.commons.model.SecHubInfrastructureScanConfiguration;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

public class InfraScanNetworkLocationProvider implements NetworkLocationProvider {

    private SecHubConfiguration config;

    public InfraScanNetworkLocationProvider(SecHubConfiguration config) {
        this.config = config;
    }

    @Override
    public List<URI> getURIs() {
        /* assert INFRASCAN configuration available */
        Optional<SecHubInfrastructureScanConfiguration> infraScan = config.getInfraScan();
        if (!infraScan.isPresent()) {
            throw new IllegalStateException("At this state there must be a infrascan setup!");
        }
        /* Fetch URI */
        SecHubInfrastructureScanConfiguration infraScanConfiguration = infraScan.get();
        List<URI> uris = infraScanConfiguration.getUris();
        if (uris == null) {
            throw new IllegalStateException("At this state URIs must be set - validation failed!");
        }
        return uris;
    }

    @Override
    public List<InetAddress> getInetAddresses() {
        if (config == null) {
            return Collections.emptyList();
        }
        Optional<SecHubInfrastructureScanConfiguration> infraScan = config.getInfraScan();
        if (!infraScan.isPresent()) {
            return Collections.emptyList();
        }
        return infraScan.get().getIps();
    }

}
