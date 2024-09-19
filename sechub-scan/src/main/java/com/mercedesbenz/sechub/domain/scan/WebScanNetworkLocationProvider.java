// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.net.InetAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

public class WebScanNetworkLocationProvider implements NetworkLocationProvider {

    private SecHubConfiguration config;

    public WebScanNetworkLocationProvider(SecHubConfiguration config) {
        this.config = config;
    }

    @Override
    public List<URI> getURIs() {
        /* assert WEBSCAN configuration available */
        Optional<SecHubWebScanConfiguration> webscan = config.getWebScan();
        if (!webscan.isPresent()) {
            throw new IllegalStateException("At this state there must be a webscan setup!");
        }
        /* Fetch URL */
        SecHubWebScanConfiguration secHubWebScanConfiguration = webscan.get();

        URI uri = secHubWebScanConfiguration.getUrl();
        if (uri == null) {
            throw new IllegalStateException("At this state the URI must be set - validation failed!");
        }

        return Arrays.asList(uri);

    }

    @Override
    public List<InetAddress> getInetAddresses() {
        /*
         * SecHubWebScanConfiguration configuration currently has no IPs inside, so we
         * do not provide this
         */
        return Collections.emptyList();
    }

}
