// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import java.net.InetAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.domain.scan.InstallSetup;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.configuration.SecHubInfrastructureScanConfiguration;

public abstract class AbstractInfrastructureScanProductExecutor<S extends InstallSetup> extends AbstractInstallSetupProductExecutor<S> implements InfrastructureScanProductExecutor {

	@Override
	protected List<URI> resolveURIsForTarget(SecHubConfiguration config) {
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
    protected List<InetAddress> resolveInetAdressForTarget(SecHubConfiguration config) {
        if (config == null) {
            return Collections.emptyList();
        }
        Optional<SecHubInfrastructureScanConfiguration> infraScan = config.getInfraScan();
        if (!infraScan.isPresent()) {
            return Collections.emptyList();
        }
        return infraScan.get().getIps();
    }
	

	@Override
	protected ScanType getScanType() {
		return ScanType.INFRA_SCAN;
	}

}
