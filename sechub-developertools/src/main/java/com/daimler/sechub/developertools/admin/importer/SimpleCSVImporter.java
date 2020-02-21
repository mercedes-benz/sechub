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

		List<CSVRow> list = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(file));) {
			String line = null;
			int lineNr=0;
			while ((line = br.readLine()) != null) {
				lineNr++;

				CSVRow row = CSVRow.importRow(line);
				int columnCount = row.columns.size();
				if (columnCount!=expectedColumnCount) {
					throw new IllegalStateException("Expected "+expectedColumnCount+" but got "+columnCount+" in line:"+lineNr);
				}
				if (lineNr>headlines) {
					list.add(row);
				}
			}
		}
		return list;

	}
	
}
