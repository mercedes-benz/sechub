// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.config;

import java.util.ArrayList;
import java.util.List;

import com.daimler.sechub.sharedkernel.mapping.MappingData;
import com.daimler.sechub.sharedkernel.mapping.MappingEntry;

public class MappingDataToNamePatternToIdEntryConverter {

    public List<NamePatternToIdEntry> convert(MappingData data) {
        List<NamePatternToIdEntry> list = new ArrayList<>();
        if (data == null) {
            return list;
        }
        
        for (MappingEntry mappingEntry : data.getEntries()) {
            
            NamePatternToIdEntry namePattternToIdEntry = new NamePatternToIdEntry(mappingEntry.getPattern(), mappingEntry.getReplacement());
            list.add(namePattternToIdEntry);
        }
        
        return list;
    }
}
