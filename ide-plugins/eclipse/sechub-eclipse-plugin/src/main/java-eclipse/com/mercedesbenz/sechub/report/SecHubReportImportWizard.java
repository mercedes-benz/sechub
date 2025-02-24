// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.report;

import java.io.File;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import com.mercedesbenz.sechub.SecHubActivator;

public class SecHubReportImportWizard extends Wizard implements IImportWizard {

	SecHubReportImportWizardPage mainPage;

	public SecHubReportImportWizard() {
		super();
	}

	public boolean performFinish() {
		File reportFile = mainPage.getFile();
		
		SecHubActivator.getDefault().getImporter().importAndDisplayReport(reportFile);
		
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("File Import Wizard"); // NON-NLS-1
		setNeedsProgressMonitor(true);
		mainPage = new SecHubReportImportWizardPage("Import SecHub Job"); // NON-NLS-1
	}

	public void addPages() {
		super.addPages();
		addPage(mainPage);
	}

}
