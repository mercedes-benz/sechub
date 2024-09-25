// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.config;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.mapping.NamePatternIdProvider;
import com.mercedesbenz.sechub.commons.mapping.NamePatternToIdEntry;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.mapping.MappingIdentifier;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdmiUpdatesMappingConfiguration;

/**
 * The scan config service represents a global scan configuration which can
 * contain for example mappings
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class ScanMappingConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(ScanMappingConfigurationService.class);

    private NamePatternIdProvider fallbackProvider = new NamePatternIdProvider(null);
    private Map<String, NamePatternIdProvider> providers = new TreeMap<>();

    @Autowired
    ScanMappingToScanMappingConfigurationTransformer transformer;

    @Autowired
    ScanMappingRepository repository;

    ScanMappingConfiguration config;

    @UseCaseAdmiUpdatesMappingConfiguration(@Step(number = 6, name = "Service call", description = "Checks if current mappings in DB lead to a new scan configuration."))
    public void refreshScanConfigIfNecessary() {
        List<ScanMapping> all = repository.findAll();
        ScanMappingConfiguration scanConfig = transformer.transform(all);
        switchConfigurationIfChanged(scanConfig);

    }

    /**
     * Get provider to resolve IDs by a given name
     *
     * @param namePatternMappingId
     * @return provider, never <code>null</code>
     */
    public NamePatternIdProvider getNamePatternIdProvider(MappingIdentifier identifier) {
        return getNamePatternIdProvider(identifier.getId());
    }

    NamePatternIdProvider getNamePatternIdProvider(String namePatternMappingId) {
        synchronized (providers) {
            NamePatternIdProvider provider = providers.get(namePatternMappingId);
            if (provider != null) {
                return provider;
            }
            return fallbackProvider;
        }
    }

    void switchConfigurationIfChanged(ScanMappingConfiguration config) {
        if (config == null) {
            return;
        }
        if (config.equals(this.config)) {
            LOG.trace("same scan configuration detected");
            return;
        }
        switchToNewConfiguration(config);

    }

    private void switchToNewConfiguration(ScanMappingConfiguration config) {
        synchronized (providers) {
            this.config = config;
            LOG.info("rebuilding providers");
            if (LOG.isDebugEnabled()) {
                LOG.debug("scan configuration rebuild json: {}", config.toJSON());
            }
            providers.clear();

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
                NamePatternIdProvider provider = new NamePatternIdProvider(providerId);
                LOG.debug("Created NamePatternIdProvider:{}", provider.getProviderId());

                List<NamePatternToIdEntry> data = configMappings.get(key);
                for (NamePatternToIdEntry entry : data) {
                    provider.add(entry);
                }
                providers.put(providerId, provider);
            }
        }
    }

    public List<NamePatternToIdEntry> getNamePatternToIdEntriesOrNull(String mappingId) {
        Map<String, List<NamePatternToIdEntry>> configMappings = config.getNamePatternMappings();
        return configMappings.get(mappingId);
    }

}
