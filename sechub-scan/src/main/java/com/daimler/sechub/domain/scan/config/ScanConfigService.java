package com.daimler.sechub.domain.scan.config;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.MustBeDocumented;

@Service
public class ScanConfigService {

	@Value("${sechub.scan.config:{}}")
	@MustBeDocumented("Injected scan config json.")
	String injectedScanConfigJSON;

	private NamePatternIdprovider fallbackProvider = new NamePatternIdprovider();
	private Map<String, NamePatternIdprovider> providers = new TreeMap<>();

	@PostConstruct
	public void postConstruct() {
		ScanConfig config = ScanConfig.createFromJSON(injectedScanConfigJSON);

		/* build providers */
		Map<String, List<NamePatternToIdEntry>> map = config.getNamePatternMappings();
		for (String key : map.keySet()) {
			NamePatternIdprovider provider = new NamePatternIdprovider();
			providers.put(key, provider);
			List<NamePatternToIdEntry> data = map.get(key);
			for (NamePatternToIdEntry entry : data) {
				provider.add(entry);
			}
		}
	}

	/**
	 * Get provider to resolve IDs by a given name
	 * @param namePatternMappingId
	 * @return provider, never <code>null</code>
	 */
	public NamePatternIdprovider getNamePatternIdProvider(String namePatternMappingId) {
		NamePatternIdprovider provider = providers.get(namePatternMappingId);
		if (provider != null) {
			return provider;
		}
		return fallbackProvider;
	}

}
