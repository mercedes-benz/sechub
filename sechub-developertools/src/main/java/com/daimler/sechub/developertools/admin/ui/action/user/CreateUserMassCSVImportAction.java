// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.user;

import java.awt.event.ActionEvent;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.developertools.admin.importer.CreateUserMassCSVImporter;
import com.daimler.sechub.developertools.admin.ui.ConfigurationSetup;
import com.daimler.sechub.developertools.admin.ui.DialogUI;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;

public class CreateUserMassCSVImportAction extends AbstractUIAction {

    private static final Logger LOG = LoggerFactory.getLogger(CreateUserMassCSVImportAction.class);

    private static final long serialVersionUID = 1L;
    private CreateUserMassCSVImporter csvImport;

    public CreateUserMassCSVImportAction(UIContext context) {
        super("Create users + accept signups by CSV import", context);
        csvImport = new CreateUserMassCSVImporter(context.getAdministration());
    }

    @Override
    public void execute(ActionEvent e) {
        DialogUI dialogUI = getContext().getDialogUI();
        File file = dialogUI.selectFile(ConfigurationSetup.SECHUB_MASS_OPERATION_PARENTDIRECTORY.getStringValue("unknown"));
        if (file == null) {
            outputAsTextOnSuccess("No file selected - canceled");
            return;
        }
        String env = ConfigurationSetup.SECHUB_ADMIN_ENVIRONMENT.getStringValue("UNKNOWN");
        if (!confirm("Do you really want to start mass user creation into environment: " + env)) {
            outputAsTextOnSuccess("Canceled mass user import");
            return;
        }
        try {
            csvImport.importUsersAndRelationsByCSV(file);
            outputAsTextOnSuccess("Mass user creation by file" + file + " successfully done");
        } catch (Exception ex) {
            outputAsTextOnSuccess("Was not able to do mass user creation by CSV import:" + ex.getMessage());
            LOG.error("Was not able to import", ex);
        }
    }

}