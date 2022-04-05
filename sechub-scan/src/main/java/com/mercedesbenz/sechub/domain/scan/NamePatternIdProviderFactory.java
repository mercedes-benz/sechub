// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.util.List;

import com.mercedesbenz.sechub.domain.scan.config.MappingDataToNamePatternToIdEntryConverter;
import com.mercedesbenz.sechub.domain.scan.config.NamePatternIdProvider;
import com.mercedesbenz.sechub.domain.scan.config.NamePatternToIdEntry;
import com.mercedesbenz.sechub.sharedkernel.mapping.MappingData;

public class NamePatternIdProviderFactory {

    MappingDataToNamePatternToIdEntryConverter converter;

    public NamePatternIdProviderFactory() {
        converter = new MappingDataToNamePatternToIdEntryConverter();
    }

    public NamePatternIdProvider createProvider(String id, String parameterValue) {
        NamePatternIdProvider provider;
        MappingData mappingData = MappingData.fromString(parameterValue);
        List<NamePatternToIdEntry> entries = converter.convert(mappingData);

        provider = new NamePatternIdProvider(id);

        for (NamePatternToIdEntry entry : entries) {
            provider.add(entry);
        }
        return provider;
    }
}
