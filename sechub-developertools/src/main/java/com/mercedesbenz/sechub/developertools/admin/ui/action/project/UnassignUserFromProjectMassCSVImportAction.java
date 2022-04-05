// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.project;

import java.awt.event.ActionEvent;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.developertools.admin.importer.UnassignUserToProjectMassCSVImporter;
import com.mercedesbenz.sechub.developertools.admin.ui.ConfigurationSetup;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;

public class UnassignUserFromProjectMassCSVImportAction extends AbstractUIAction {

    private static final Logger LOG = LoggerFactory.getLogger(UnassignUserFromProjectMassCSVImportAction.class);

    private static final long serialVersionUID = 1L;
    private UnassignUserToProjectMassCSVImporter csvImport;

    public UnassignUserFromProjectMassCSVImportAction(UIContext context) {
        super("Unassign user from projects by CSV import", context);

        csvImport = new UnassignUserToProjectMassCSVImporter(context.getAdministration());
    }

    @Override
    public void execute(ActionEvent e) {
        File file = getContext().getDialogUI().selectFile(ConfigurationSetup.SECHUB_MASS_OPERATION_PARENTDIRECTORY.getStringValue("unknown"));
        if (file == null) {
            outputAsTextOnSuccess("No file selected - canceled");
            return;
        }
        String env = ConfigurationSetup.SECHUB_ADMIN_ENVIRONMENT.getStringValue("UNKNOWN");
        if (!confirm("Do you really want to start mass user2project un-assignment into environment: " + env)) {
            outputAsTextOnSuccess("Canceled mass userFromproject unassign import");
            return;
        }
        try {
            csvImport.importUsersFromProjectUnassignmentsByCSV(file);
            outputAsTextOnSuccess("Mass user2project un-assignment by file" + file + " successfully done");
        } catch (Exception ex) {
            outputAsTextOnSuccess("Was not able to do mass userFromproject un-assignment by CSV import:" + ex.getMessage());
            LOG.error("Was not able to import", ex);
        }
    }

}