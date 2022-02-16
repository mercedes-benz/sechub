// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.importer;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.mercedesbenz.sechub.developertools.admin.DeveloperAdministration;

public class AssignUserToProjectMassCSVImporter {

    private SimpleCSVImporter csvImporter = new SimpleCSVImporter();
    private DeveloperAdministration administration;

    public AssignUserToProjectMassCSVImporter(DeveloperAdministration administration) {
        this.administration = administration;
    }

    public void importUsersToProjectAssignmentsByCSV(File file) throws IOException {
        List<CSVRow> rows = csvImporter.importCSVFile(file, 2, 1);

        for (CSVRow row : rows) {
            importRow(row);
        }

    }

    private void importRow(CSVRow row) {
        Iterator<CSVColumn> it = row.columns.iterator();
        String projectId = it.next().cell.trim();
        String users = it.next().cell.trim();

        if (users.isEmpty()) {
            return;
        }
        for (String userId : users.split(",")) {
            administration.assignUserToProject(userId.trim().toLowerCase(), projectId);
        }
    }

}
