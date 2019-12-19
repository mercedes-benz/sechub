// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.config;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.MustBeDocumented;

/**
 * Provides access to scan config parts in a convenient way.
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class ScanConfigService {

	private static final Logger LOG = LoggerFactory.getLogger(ScanConfigService.class);

	@Value("${sechub.scan.config.initial:{}}")
	@MustBeDocumented(value = "Initial scan config as JSON value. *Initial* means that this configuration is only for server start time when no scan configuration is available.", secret = true, scope = "config")
	String scanConfigJSON;

	private NamePatternIdprovider fallbackProvider = new NamePatternIdprovider(null);
	private Map<String, NamePatternIdprovider> providers = new TreeMap<>();

	@PostConstruct
	public void postConstruct() {
		rebuildScanConfiguration();
	}

	/*
	 * At the moment, this method is only used by integration test API. When we
	 * implement https://github.com/Daimler/sechub/issues/129 we can use this method
	 * as well - because then its cluster wide...
	 */
	public void updateScanConfiguration(String scanConfigAsJSON) {
		if (scanConfigAsJSON == null) {
			return;
		}
		if (scanConfigAsJSON.equals(this.scanConfigJSON)) {
			return;
		}
		this.scanConfigJSON = scanConfigAsJSON;
		rebuildScanConfiguration();
	}

	private void rebuildScanConfiguration() {
		synchronized (providers) {
			LOG.info("start configuration creation");
			LOG.debug("scan configuration uses json: {}", scanConfigJSON);
			providers.clear();

			ScanConfig config = ScanConfig.createFromJSON(scanConfigJSON);

			/* build providers */
			Map<String, List<NamePatternToIdEntry>> configMappings = config.getNamePatternMappings();
			for (String key : configMappings.keySet()) {
				if (key == null) {
					/* We have a tree map - so null is not allowed here. */
					LOG.warn("scan config contains null key - is ignored");
					continue;
				}
				String providerId = key.trim();
				if (providerId.isEmpty()) {
					/* We do also not accept empty name patterns */
					LOG.warn("scan config contains empty key - is ignored");
					continue;
				}
				NamePatternIdprovider provider = new NamePatternIdprovider(providerId);
				providers.put(providerId, provider);
				LOG.debug("Created NamePatternIdprovider:{}", provider.getProviderId());

				List<NamePatternToIdEntry> data = configMappings.get(key);
				for (NamePatternToIdEntry entry : data) {
					provider.add(entry);
				}
			}
		}
	}

	/**
	 * Get provider to resolve IDs by a given name
	 *
	 * @param namePatternMappingId
	 * @return provider, never <code>null</code>
	 */
	public NamePatternIdprovider getNamePatternIdProvider(String namePatternMappingId) {
		synchronized (providers) {
			NamePatternIdprovider provider = providers.get(namePatternMappingId);
			if (provider != null) {
				return provider;
			}
			return fallbackProvider;
		}
	}

}
