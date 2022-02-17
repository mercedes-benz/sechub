// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.importer;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.mercedesbenz.sechub.developertools.admin.DeveloperAdministration;

public class DeleteProjectMassCSVImporter {

    private SimpleCSVImporter csvImporter = new SimpleCSVImporter();
    private DeveloperAdministration administration;

    public DeleteProjectMassCSVImporter(DeveloperAdministration administration) {
        this.administration = administration;
    }

    public void importProjectDeletesByCSV(File file) throws IOException {
        List<CSVRow> rows = csvImporter.importCSVFile(file, 1, 1);

        for (CSVRow row : rows) {
            importRow(row);
        }

    }

    private void importRow(CSVRow row) {
        Iterator<CSVColumn> it = row.columns.iterator();
        String projectId = it.next().cell.trim();

        administration.deleteProject(projectId);
    }

}
