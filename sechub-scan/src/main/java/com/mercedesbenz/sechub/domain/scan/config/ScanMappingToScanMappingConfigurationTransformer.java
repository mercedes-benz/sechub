// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.config;

import java.util.List;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.mapping.MappingData;
import com.mercedesbenz.sechub.commons.mapping.MappingDataToNamePatternToIdEntryConverter;

@Component
public class ScanMappingToScanMappingConfigurationTransformer {

    MappingDataToNamePatternToIdEntryConverter converter;

    public ScanMappingToScanMappingConfigurationTransformer() {
        converter = new MappingDataToNamePatternToIdEntryConverter();
    }

    public ScanMappingConfiguration transform(List<ScanMapping> mappings) {
        ScanMappingConfiguration config = new ScanMappingConfiguration();

        if (mappings == null || mappings.size() == 0) {
            return config;
        }

        for (ScanMapping mapping : mappings) {
            MappingData data = MappingData.fromString(mapping.getData());
            config.getNamePatternMappings().put(mapping.getId(), converter.convert(data));
        }

        return config;
    }

}
