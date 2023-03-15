// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.importer;

import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.sharedkernel.mapping.MappingData;
import com.mercedesbenz.sechub.sharedkernel.mapping.MappingEntry;

public class MappingDataCSVSupport {

    public List<CSVRow> toCSVRows(MappingData data) {
        List<CSVRow> rows = new ArrayList<>();
        CSVRow headline = new CSVRow();
        rows.add(headline);
        headline.add("Pattern");
        headline.add("Replacement");
        headline.add("Comment");
        for (MappingEntry entry : data.getEntries()) {
            CSVRow row = new CSVRow();
            rows.add(row);
            row.add(entry.getPattern());
            row.add(entry.getReplacement());
            row.add(entry.getComment());
        }
        return rows;
    }

    public MappingData fromCSVRows(List<CSVRow> rows, int headlines) {
        MappingData data = new MappingData();
        if (rows.size() <= headlines) {
            throw new IllegalStateException("Row count must be > headline count:" + headlines);
        }
        int count = 0;
        for (CSVRow row : rows) {
            count++;
            if (count <= headlines) {
                /* ignore headline */
                continue;
            }
            if (row.columns.size() != 3) {
                throw new IllegalStateException("column count must be 3, but is:" + row.columns.size());
            }
            int col = 0;
            String pattern = row.columns.get(col++).cell;
            String replacement = row.columns.get(col++).cell;
            String comment = row.columns.get(col++).cell;

            data.getEntries().add(new MappingEntry(pattern, replacement, comment));
        }
        return data;
    }
}
