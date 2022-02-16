// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.importer;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.developertools.admin.DeveloperAdministration;

/**
 * Format example available as
 * `example5-developer-admin-ui_mass-import_users.csv`:
 *
 * <pre>
 * UserId;Email;auto-accept
 * import-user1;import-user1@example.com;true
 * import-user2;import-user2@example.com;true
 * import-user3;import-user3@example.com;false
 * accept-signup-user4;;true
 * accept-signup-user5;;false
 * </pre>
 *
 * <ol>
 * <li>user1, user2 a signup is done + automatically accepted</li>
 * <li>user3 a signup is done, but accept is not done, because explicit disabled
 * <li>user4 has no mail (means already signup done) and will just be
 * accepted</li>
 * <li>user5 has no mail (means already signup done) but will not be accepted,
 * so means nothing done</li>
 * </ol>
 *
 *
 *
 *
 * @author Albert Tregnaghi
 *
 */
public class CreateUserMassCSVImporter {

    private static final Logger LOG = LoggerFactory.getLogger(CreateUserMassCSVImporter.class);

    private SimpleCSVImporter csvImporter = new SimpleCSVImporter();
    private DeveloperAdministration administration;

    public CreateUserMassCSVImporter(DeveloperAdministration administration) {
        this.administration = administration;
    }

    public void importUsersAndRelationsByCSV(File file) throws IOException {
        List<CSVRow> rows = csvImporter.importCSVFile(file, 3, 1);

        for (CSVRow row : rows) {
            importRow(row);
        }

    }

    private void importRow(CSVRow row) {
        Iterator<CSVColumn> it = row.columns.iterator();
        String userId = it.next().cell.trim();
        String email = it.next().cell.trim();

        String acceptAsText = it.next().cell.trim();
        boolean accept = Boolean.valueOf(acceptAsText);
        if (userId.isEmpty()) {
            LOG.error("no user id found - ignore!");
            return;
        }
        if (!email.isEmpty()) {
            administration.createNewUserSignup(userId, email);
        }
        if (!accept) {
            return;
        }

        administration.acceptSignup(userId);
    }

}
