// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import java.net.InetAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.commons.model.SecHubWebScanConfiguration;
import com.daimler.sechub.domain.scan.InstallSetup;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;

public abstract class AbstractWebScanProductExecutor<S extends InstallSetup> extends AbstractInstallSetupProductExecutor<S> implements WebScanProductExecutor {

	@Override
	protected List<URI> resolveURIsForTarget(SecHubConfiguration config) {
		/* assert WEBSCAN configuration available */
		Optional<SecHubWebScanConfiguration> webscan = config.getWebScan();
		if (!webscan.isPresent()) {
			throw new IllegalStateException("At this state there must be a webscan setup!");
		}
		/* Fetch URL */
		SecHubWebScanConfiguration secHubWebScanConfiguration = webscan.get();
		
		URI uri = secHubWebScanConfiguration.getUri();
		if (uri == null) {
			throw new IllegalStateException("At this state the URI must be set - validation failed!");
		}

		return Arrays.asList(uri);
	}
	
    protected URI resolveURIForTarget(SecHubConfiguration config) {
        /* assert WEBSCAN configuration available */
        Optional<SecHubWebScanConfiguration> webscan = config.getWebScan();
        if (!webscan.isPresent()) {
            throw new IllegalStateException("At this state there must be a webscan setup!");
        }
        /* Fetch URL */
        SecHubWebScanConfiguration secHubWebScanConfiguration = webscan.get();
        
        URI uri = secHubWebScanConfiguration.getUri();
        
        if (uri == null) {
            throw new IllegalStateException("At this state the URI must be set - validation failed!");
        }

        return uri;
    }
	
	@Override
	protected List<InetAddress> resolveInetAdressForTarget(SecHubConfiguration config) {
		/* SecHubWebScanConfiguration configuration currently has no IPs inside, so we do not provide this */
		return Collections.emptyList();
	}


	@Override
	protected ScanType getScanType() {
		return ScanType.WEB_SCAN;
	}

}
