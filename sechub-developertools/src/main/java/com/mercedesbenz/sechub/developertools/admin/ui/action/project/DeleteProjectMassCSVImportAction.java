// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.project;

import java.awt.event.ActionEvent;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.developertools.admin.importer.DeleteProjectMassCSVImporter;
import com.mercedesbenz.sechub.developertools.admin.ui.ConfigurationSetup;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;

public class DeleteProjectMassCSVImportAction extends AbstractUIAction {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteProjectMassCSVImportAction.class);

    private static final long serialVersionUID = 1L;
    private DeleteProjectMassCSVImporter csvImport;

    public DeleteProjectMassCSVImportAction(UIContext context) {
        super("Delete projects by CSV import", context);
        csvImport = new DeleteProjectMassCSVImporter(context.getAdministration());
    }

    @Override
    public void execute(ActionEvent e) {
        File file = getContext().getDialogUI().selectFile(ConfigurationSetup.SECHUB_MASS_OPERATION_PARENTDIRECTORY.getStringValue("unknown"));
        if (file == null) {
            outputAsTextOnSuccess("No file selected - canceled");
            return;
        }
        String env = ConfigurationSetup.SECHUB_ADMIN_ENVIRONMENT.getStringValue("UNKNOWN");
        if (!confirm("Do you really want to start mass project deletion from environment: " + env)) {
            outputAsTextOnSuccess("Canceled mass project delete import");
            return;
        }
        try {
            csvImport.importProjectDeletesByCSV(file);
            outputAsTextOnSuccess("Mass delete by file" + file + " successfully done");
        } catch (Exception ex) {
            outputAsTextOnSuccess("Was not able to do mass delete:" + ex.getMessage());
            LOG.error("Was not able to import", ex);
        }
    }

}