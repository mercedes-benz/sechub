// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.report;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.mercedesbenz.sechub.SecHubActivator;
import com.mercedesbenz.sechub.component.FileChooser;

/**
 * The import dialog.
 * 
 * The import dialog uses the file chooser component 
 * to let the user select a file from the local file system.
 */
public class SecHubReportImportDialog extends Dialog {

	private FileChooser fileChooser;

	public SecHubReportImportDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);

		fileChooser = new FileChooser(container);

		return container;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Import SecHub Report");
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Import", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void okPressed() {
		File reportFile = fileChooser.getFile();

		SecHubActivator.getDefault().getImporter().importAndDisplayReport(reportFile);
		
		super.okPressed();
	}


}