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
	public List<ImportCSVRow> importCSVFile(File file, int expectedColumnCount, int headlines) throws IOException {

		List<ImportCSVRow> list = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(file));) {
			String line = null;
			int lineNr=0;
			while ((line = br.readLine()) != null) {
				lineNr++;

				ImportCSVRow row = importRow(line);
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

	private ImportCSVRow importRow(String line) {
		ImportCSVRow row = new ImportCSVRow();
		String[] splitted = line.split(";");
		for (String splitPart : splitted) {
			row.add(splitPart);
		}
		return row;
	}
}
