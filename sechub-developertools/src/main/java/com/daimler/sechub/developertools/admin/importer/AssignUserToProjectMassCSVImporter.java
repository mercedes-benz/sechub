// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.importer;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.daimler.sechub.developertools.admin.DeveloperAdministration;

public class AssignUserToProjectMassCSVImporter {

	private SimpleCSVImporter csvImporter = new SimpleCSVImporter();
	private DeveloperAdministration administration;


	public AssignUserToProjectMassCSVImporter(DeveloperAdministration administration) {
		this.administration=administration;
	}

	public void importUsersToProjectAssignmentsByCSV(File file) throws IOException {
		List<ImportCSVRow> rows = csvImporter.importCSVFile(file, 2, 1);

		for (ImportCSVRow row: rows) {
			importRow(row);
		}

	}

	private void importRow(ImportCSVRow row) {
		Iterator<ImportCSVColumn> it = row.columns.iterator();
		String projectId = it.next().cell.trim();
		String users = it.next().cell.trim();

		if (users.isEmpty()) {
			return;
		}
		for (String userId: users.split(",")) {
			administration.assignUserToProject(userId.trim(), projectId);
		}
	}

}
