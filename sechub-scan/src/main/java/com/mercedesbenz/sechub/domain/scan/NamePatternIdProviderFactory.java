// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.util.List;

import com.mercedesbenz.sechub.domain.scan.config.MappingDataToNamePatternToIdEntryConverter;
import com.mercedesbenz.sechub.domain.scan.config.NamePatternIdprovider;
import com.mercedesbenz.sechub.domain.scan.config.NamePatternToIdEntry;
import com.mercedesbenz.sechub.sharedkernel.mapping.MappingData;

public class NamePatternIdProviderFactory {

    MappingDataToNamePatternToIdEntryConverter converter;

    public NamePatternIdProviderFactory() {
        converter = new MappingDataToNamePatternToIdEntryConverter();
    }

    public NamePatternIdprovider createProvider(String id, String parameterValue) {
        NamePatternIdprovider provider;
        MappingData mappingData = MappingData.fromString(parameterValue);
        List<NamePatternToIdEntry> entries = converter.convert(mappingData);

        provider = new NamePatternIdprovider(id);

        for (NamePatternToIdEntry entry : entries) {
            provider.add(entry);
        }
        return provider;
    }
}
