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

            NamePatternToIdEntry namePattternToIdEntry = new NamePatternToIdEntry(mappingEntry.getPattern(), mappingEntry.getReplacement());
            list.add(namePattternToIdEntry);
        }

        return list;
    }
}
