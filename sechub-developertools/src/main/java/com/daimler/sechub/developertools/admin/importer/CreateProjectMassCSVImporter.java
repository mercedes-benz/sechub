// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.importer;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.daimler.sechub.developertools.admin.DeveloperAdministration;

public class CreateProjectMassCSVImporter {

	private SimpleCSVImporter csvImporter = new SimpleCSVImporter();
	private DeveloperAdministration administration;


	public CreateProjectMassCSVImporter(DeveloperAdministration administration) {
		this.administration=administration;
	}

	public void importProjectsAndRelationsByCSV(File file) throws IOException {
		List<CSVRow> rows = csvImporter.importCSVFile(file, 3, 1);

		for (CSVRow row: rows) {
			importRow(row);
		}

	}

	private void importRow(CSVRow row) {
		Iterator<CSVColumn> it = row.columns.iterator();
		String projectId = it.next().cell.trim();
		String owner = it.next().cell.trim();
		String users = it.next().cell.trim();

		administration.createProject(projectId, "Project "+projectId, owner, Collections.emptyList());
		if (users.isEmpty()) {
			return;
		}
		for (String userId: users.split(",")) {
			administration.assignUserToProject(userId.trim(), projectId);
		}
	}

}
