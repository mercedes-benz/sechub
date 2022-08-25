// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.mapping;

import java.util.List;

public class NamePatternIdProviderFactory {

    MappingDataToNamePatternToIdEntryConverter converter;

    public NamePatternIdProviderFactory() {
        converter = new MappingDataToNamePatternToIdEntryConverter();
    }

    public NamePatternIdProvider createProvider(String id, String mappingDataAsJson) {
        NamePatternIdProvider provider;
        MappingData mappingData = MappingData.fromString(mappingDataAsJson);
        List<NamePatternToIdEntry> entries = converter.convert(mappingData);

        provider = new NamePatternIdProvider(id);

        for (NamePatternToIdEntry entry : entries) {
            provider.add(entry);
        }
        return provider;
    }
}
