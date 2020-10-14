// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimpleCSVImporter {

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
    public List<CSVRow> importCSVFile(File file, int expectedColumnCount, int headlines) throws IOException {
        return importCSVFile(file, expectedColumnCount, headlines, true);
    }

    /**
     * Imports given file as a list of rows. When amount columns is different, there
     * will be an illegal state exception thrown!
     *
     * @param file
     * @param maxColumns
     * @param headlines
     * @param insistColumnsAllSame when <code>true</code> data rows must contain all
     *                             content, if <code>false</code> then only headline
     *                             will be checked to contain expected max amount of
     *                             columns and content can have less (so optional) columns- but
     *                             when column count is greater than expected max
     *                             column this will also throw an error
     * @return
     * @throws IOException
     */
    public List<CSVRow> importCSVFile(File file, int maxColumns, int headlines, boolean insistColumnsAllSame) throws IOException {

        List<CSVRow> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file));) {
            String line = null;
            int lineNr = 0;
            while ((line = br.readLine()) != null) {
                lineNr++;
                CSVRow row = CSVRow.importRow(line);
                
                int columnCount = row.columns.size();
                if (columnCount > maxColumns) {
                    throw new IllegalStateException("Column count expected:" + maxColumns + " but got " + columnCount + " in line:" + lineNr);
                }

                boolean isHeadline = lineNr <= headlines;
                boolean checkNecessary = insistColumnsAllSame || isHeadline;

                if (checkNecessary) {
                    if (columnCount != maxColumns) {
                        throw new IllegalStateException("Expected " + maxColumns + " but got " + columnCount + " in line:" + lineNr);
                    }
                }
                if (!isHeadline) {
                    list.add(row);
                }
            }
        }
        return list;

    }

}
