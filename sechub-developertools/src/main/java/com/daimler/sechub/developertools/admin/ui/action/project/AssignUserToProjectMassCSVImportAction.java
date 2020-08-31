// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.project;

import java.awt.event.ActionEvent;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.developertools.admin.importer.AssignUserToProjectMassCSVImporter;
import com.daimler.sechub.developertools.admin.ui.ConfigurationSetup;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;

public class AssignUserToProjectMassCSVImportAction extends AbstractUIAction {


	private static final Logger LOG = LoggerFactory.getLogger(AssignUserToProjectMassCSVImportAction.class);

	private static final long serialVersionUID = 1L;
	private AssignUserToProjectMassCSVImporter csvImport;

	public AssignUserToProjectMassCSVImportAction(UIContext context) {
		super("Assign user to projects by CSV import",context);
		
		csvImport = new AssignUserToProjectMassCSVImporter(context.getAdministration());
	}

	@Override
	public void execute(ActionEvent e) {
		File file = getContext().getDialogUI().selectFile(ConfigurationSetup.SECHUB_MASS_OPERATION_PARENTDIRECTORY.getStringValue("unknown"));
		if (file==null) {
			outputAsTextOnSuccess("No file selected - canceled");
			return;
		}
		String env = ConfigurationSetup.SECHUB_ADMIN_ENVIRONMENT.getStringValue("UNKNOWN");
		if (! confirm("Do you really want to start mass user2project assignment into environment: "+env)) {
			outputAsTextOnSuccess("Canceled mass user2project import");
			return;
		}
		try {
			csvImport.importUsersToProjectAssignmentsByCSV(file);
			outputAsTextOnSuccess("Mass user2project assignment by file"+file+" successfully done");
		} catch (Exception ex) {
			outputAsTextOnSuccess("Was not able to do mass user2project assignment by CSV import:"+ex.getMessage());
			LOG.error("Was not able to import",ex);
		}
	}

}