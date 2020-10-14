// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.project;

import java.awt.event.ActionEvent;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.developertools.admin.importer.CreateProjectMassCSVImporter;
import com.daimler.sechub.developertools.admin.ui.ConfigurationSetup;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;

public class CreateProjectMassCSVImportAction extends AbstractUIAction {

    private static final Logger LOG = LoggerFactory.getLogger(CreateProjectMassCSVImportAction.class);

    private static final long serialVersionUID = 1L;
    private CreateProjectMassCSVImporter csvImport;

    public CreateProjectMassCSVImportAction(UIContext context) {
        super("Create projects by CSV import", context);
        csvImport = new CreateProjectMassCSVImporter(context.getAdministration());
    }

    @Override
    public void execute(ActionEvent e) {
        File file = getContext().getDialogUI().selectFile(ConfigurationSetup.SECHUB_MASS_OPERATION_PARENTDIRECTORY.getStringValue("unknown"));
        if (file == null) {
            outputAsTextOnSuccess("No file selected - canceled");
            return;
        }
        String env = ConfigurationSetup.SECHUB_ADMIN_ENVIRONMENT.getStringValue("UNKNOWN");
        if (!confirm("Do you really want to start mass project creation into environment: " + env)) {
            outputAsTextOnSuccess("Canceled mass project import");
            return;
        }
        try {
            csvImport.importProjectsAndRelationsByCSV(file);
            outputAsTextOnSuccess("Mass project creation by file" + file + " successfully done");
        } catch (Exception ex) {
            outputAsTextOnSuccess("Was not able to do mass project creation by CSV import:" + ex.getMessage());
            LOG.error("Was not able to import", ex);
        }
    }

}