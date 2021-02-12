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
        for (MappingEntry entry : data.getEntries()) {
            NamePatternToIdEntry np = new NamePatternToIdEntry(entry.getPattern(), entry.getReplacement());
            list.add(np);
        }
        return list;
    }
}
