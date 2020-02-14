// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.importer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CSVRow {
    public static final String COMMA_SEPARATOR = ";";
    List<CSVColumn> columns = new ArrayList<>();

    public void add(String cell) {
        CSVColumn column = new CSVColumn();
        column.cell = cell;
        columns.add(column);
    }

    public static CSVRow importRow(String line) {
        CSVRow row = new CSVRow();
        String[] splitted = line.split(COMMA_SEPARATOR);
        for (String splitPart : splitted) {
            row.add(splitPart);
        }
        return row;
    }

    public String exportRow() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<CSVColumn> it = columns.iterator(); it.hasNext();) {
            CSVColumn column = it.next();
            sb.append(column.cell);
            if (it.hasNext()) {
                sb.append(COMMA_SEPARATOR);
            }
        }
        return sb.toString();
    }
}
