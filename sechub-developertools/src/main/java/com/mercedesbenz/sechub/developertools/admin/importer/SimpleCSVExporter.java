// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.importer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class SimpleCSVExporter {

    /**
     * Imports given file as a list of rows. When amount columns is different, there
     * will be an illegal state exception thrown!
     *
     * @param file
     * @param expectedColumnCount
     * @param headlines
     * @return
     * @throws IOException
     */
    public void exportCSVFile(File file, List<CSVRow> rows, int expectedColumnCount) throws IOException {

        StringBuilder sb = new StringBuilder();
        for (Iterator<CSVRow> it = rows.iterator(); it.hasNext();) {
            CSVRow row = it.next();
            int size = row.columns.size();
            if (size != expectedColumnCount) {
                throw new IllegalArgumentException("Column count differs. Expected was: " + expectedColumnCount + " but was:" + size);
            }
            sb.append(row.exportRow());
            if (it.hasNext()) {
                sb.append("\n");
            }
        }
        try (BufferedWriter br = new BufferedWriter(new FileWriter(file));) {
            br.write(sb.toString());
        }
    }

}
