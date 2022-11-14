// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.mapping;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamePatternIdProviderFactory {

    private static final Logger LOG = LoggerFactory.getLogger(NamePatternIdProviderFactory.class);

    private static final String EMPTY_JSON = "{}";

    MappingDataToNamePatternToIdEntryConverter converter;

    public NamePatternIdProviderFactory() {
        converter = new MappingDataToNamePatternToIdEntryConverter();
    }

    public NamePatternIdProvider createProvider(String id, String mappingDataAsJson) {
        NamePatternIdProvider provider;

        LOG.debug("Create name pattern id provider - id: '{}' , mappingData: '{}'", id, mappingDataAsJson);
        if (mappingDataAsJson == null || mappingDataAsJson.trim().isEmpty()) {

            LOG.debug("Data for '{}' was: '{}' => Will use fallback: '{}'.", id, mappingDataAsJson, EMPTY_JSON);
            mappingDataAsJson = EMPTY_JSON;
        }

        MappingData mappingData = MappingData.fromString(mappingDataAsJson);
        List<NamePatternToIdEntry> entries = converter.convert(mappingData);

        provider = new NamePatternIdProvider(id);

        for (NamePatternToIdEntry entry : entries) {
            provider.add(entry);
        }
        return provider;
    }
}
