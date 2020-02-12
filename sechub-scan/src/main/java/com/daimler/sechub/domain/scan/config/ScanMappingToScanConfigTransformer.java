package com.daimler.sechub.domain.scan.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.mapping.MappingData;
import com.daimler.sechub.sharedkernel.mapping.MappingEntry;

@Component
public class ScanMappingToScanConfigTransformer {

    public ScanConfig transform(List<ScanMapping> mappings) {
        ScanConfig config = new ScanConfig();
        
        if (mappings==null || mappings.size()==0) {
            return config;
        }
        
        for (ScanMapping mapping: mappings) {
            MappingData data = MappingData.fromString(mapping.getData());
            config.getNamePatternMappings().put(mapping.getId(), convert(data));
        }
        
        return config;
    }

    private List<NamePatternToIdEntry> convert(MappingData data) {
        List<NamePatternToIdEntry> list = new ArrayList<>();
        for (MappingEntry entry: data.getEntries()) {
            NamePatternToIdEntry np = new NamePatternToIdEntry(entry.getPattern(),entry.getReplacement());
            list.add(np);
        }
        return list;
    }
}
