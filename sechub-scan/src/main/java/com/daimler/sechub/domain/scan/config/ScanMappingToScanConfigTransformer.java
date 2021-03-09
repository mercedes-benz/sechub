// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.config;

import java.util.List;

import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.mapping.MappingData;

@Component
public class ScanMappingToScanConfigTransformer {

    MappingDataToNamePatternToIdEntryConverter converter;
    
    public ScanMappingToScanConfigTransformer(){
        converter = new MappingDataToNamePatternToIdEntryConverter();
    }
    
    public ScanConfig transform(List<ScanMapping> mappings) {
        ScanConfig config = new ScanConfig();
        
        if (mappings==null || mappings.size()==0) {
            return config;
        }
        
        for (ScanMapping mapping: mappings) {
            MappingData data = MappingData.fromString(mapping.getData());
            config.getNamePatternMappings().put(mapping.getId(), converter.convert(data));
        }
        
        return config;
    }

   
}
