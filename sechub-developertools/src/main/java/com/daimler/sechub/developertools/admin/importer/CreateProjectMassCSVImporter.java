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
        this.administration = administration;
    }

    public void importProjectsAndRelationsByCSV(File file) throws IOException {
        List<CSVRow> rows = csvImporter.importCSVFile(file, 4, 1, false);

        int rowNumber = 2;
        for (CSVRow row : rows) {
            administration.getUiContext().getOutputUI().output("starting import of CSV row:"+rowNumber);
            importRow(row, rowNumber++);
        }

    }

    private void importRow(CSVRow row, int rowNumber) {
        int size = row.columns.size();
        Iterator<CSVColumn> it = row.columns.iterator();
        if (size < 2) {
            throw new IllegalStateException("At least project id and owner must be given! But did happen in row:" + rowNumber);
        }
        CSVColumn projectColumn = it.next();
        String projectId =projectColumn.cell.trim();
        if (projectId.isEmpty()) {
            throw new IllegalStateException("A project id must be not empty! But did happen in row:" + rowNumber);
        }
        String owner = it.next().cell.trim();
        administration.createProject(projectId, "Project " + projectId, owner.trim().toLowerCase(), Collections.emptyList(), Collections.emptyMap());
        if (size == 2) {
            return;
        }
        CSVColumn userColumn = it.next();
        String users = userColumn.cell.trim();
        if (!users.isEmpty()) {
            for (String userId : users.split(",")) {
                administration.assignUserToProject(userId.trim().toLowerCase(), projectId.trim().toLowerCase());
            }
        }
        if (size == 3) {
            return;
        }
        String profiles = it.next().cell.trim();
        if (!profiles.isEmpty()) {
            for (String profileId : profiles.split(",")) {
                administration.addProjectIdsToProfile(profileId.trim().toLowerCase(), projectId.trim().toLowerCase());
            }
        }
    }

}
