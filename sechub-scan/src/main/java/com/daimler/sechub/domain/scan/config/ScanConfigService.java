// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.config;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.mapping.MappingIdentifier;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorUpdatesMappingConfiguration;

/**
 * The scan config service represents a global scan configuration which can contain for example mappings
 * @author Albert Tregnaghi
 *
 */
@Service
public class ScanConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(ScanConfigService.class);

    private NamePatternIdprovider fallbackProvider = new NamePatternIdprovider(null);
    private Map<String, NamePatternIdprovider> providers = new TreeMap<>();

    @Autowired
    ScanMappingToScanConfigTransformer transformer;

    @Autowired
    ScanMappingRepository repository;

    ScanConfig config;

    @UseCaseAdministratorUpdatesMappingConfiguration(@Step(number = 6, name = "Service call", description = "Checks if current mappings in DB lead to a new scan configuration."))
    public void refreshScanConfigIfNecessary() {
        List<ScanMapping> all = repository.findAll();
        ScanConfig scanConfig = transformer.transform(all);
        switchConfigurationIfChanged(scanConfig);

    }

    /**
     * Get provider to resolve IDs by a given name
     *
     * @param namePatternMappingId
     * @return provider, never <code>null</code>
     */
    public NamePatternIdprovider getNamePatternIdProvider(MappingIdentifier identifier) {
        return getNamePatternIdProvider(identifier.getId());
    }

    /**
     * Get provider to resolve IDs by a given name. Deprecation: This method should NOT be used
     * outside this package to avoid usage without mapping identifiers!
     *
     * @param namePatternMappingId
     * @return provider, never <code>null</code>
     */
    @Deprecated
    public NamePatternIdprovider getNamePatternIdProvider(String namePatternMappingId) {
        synchronized (providers) {
            NamePatternIdprovider provider = providers.get(namePatternMappingId);
            if (provider != null) {
                return provider;
            }
            return fallbackProvider;
        }
    }

    void switchConfigurationIfChanged(ScanConfig config) {
        if (config == null) {
            return;
        }
        if (config.equals(this.config)) {
            LOG.trace("same scan configuration detected");
            return;
        }
        switchToNewConfiguration(config);

    }

    private void switchToNewConfiguration(ScanConfig config) {
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
                NamePatternIdprovider provider = new NamePatternIdprovider(providerId);
                LOG.debug("Created NamePatternIdprovider:{}", provider.getProviderId());

                List<NamePatternToIdEntry> data = configMappings.get(key);
                for (NamePatternToIdEntry entry : data) {
                    provider.add(entry);
                }
                providers.put(providerId, provider);
            }
        }
    }

}
