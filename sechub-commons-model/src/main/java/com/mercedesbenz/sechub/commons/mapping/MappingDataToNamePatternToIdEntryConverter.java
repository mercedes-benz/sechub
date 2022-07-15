// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.mapping;

import java.util.ArrayList;
import java.util.List;

public class MappingDataToNamePatternToIdEntryConverter {

    public List<NamePatternToIdEntry> convert(MappingData data) {
        List<NamePatternToIdEntry> list = new ArrayList<>();
        if (data == null) {
            return list;
        }

        for (MappingEntry mappingEntry : data.getEntries()) {

            String pattern = mappingEntry.getPattern();
            if (pattern == null) {
                continue;
            }
            String replacement = mappingEntry.getReplacement();
            NamePatternToIdEntry namePattternToIdEntry = new NamePatternToIdEntry(pattern, replacement);
            list.add(namePattternToIdEntry);
        }

        return list;
    }
}
