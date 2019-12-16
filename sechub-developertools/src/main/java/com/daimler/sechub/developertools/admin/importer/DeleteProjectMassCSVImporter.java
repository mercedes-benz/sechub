// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.importer;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.daimler.sechub.developertools.admin.DeveloperAdministration;

public class DeleteProjectMassCSVImporter {

	private SimpleCSVImporter csvImporter = new SimpleCSVImporter();
	private DeveloperAdministration administration;


	public DeleteProjectMassCSVImporter(DeveloperAdministration administration) {
		this.administration=administration;
	}

	public void importProjectDeletesByCSV(File file) throws IOException {
		List<ImportCSVRow> rows = csvImporter.importCSVFile(file, 1, 1);

		for (ImportCSVRow row: rows) {
			importRow(row);
		}

	}

	private void importRow(ImportCSVRow row) {
		Iterator<ImportCSVColumn> it = row.columns.iterator();
		String projectId = it.next().cell;

		administration.deleteProject(projectId);
	}

}
