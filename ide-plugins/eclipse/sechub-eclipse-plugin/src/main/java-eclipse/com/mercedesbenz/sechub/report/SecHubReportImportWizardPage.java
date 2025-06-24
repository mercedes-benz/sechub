// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;

import com.mercedesbenz.sechub.component.FileChooser;

public class SecHubReportImportWizardPage extends WizardPage {

	protected FileFieldEditor editor;
	protected FileChooser fileChooser;

	public SecHubReportImportWizardPage(String pageName) {
		super(pageName);
		setTitle(pageName); // NON-NLS-1
		setDescription("Import a SecHub report from local file system"); // NON-NLS-1
	}

	protected void createAdvancedControls(Composite parent) {
		fileChooser = new FileChooser(parent);
		fileChooser.setTitle("FileChooser Title");
	}

	protected void createLinkTarget() {
	}

	protected InputStream getInitialContents() {
		try {
			return new FileInputStream(new File(editor.getStringValue()));
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	protected String getNewFileLabel() {
		return "New File Name:"; // NON-NLS-1
	}

	protected IStatus validateLinkedResource() {
		return new Status(IStatus.OK, "com.mercedesbenz.sechub.plugin", IStatus.OK, "", null); // NON-NLS-1 //NON-NLS-2
	}

	@Override
	public void createControl(Composite parent) {
		
		RowLayout layout = new RowLayout();

		// top level group
		Composite topLevel = new Composite(parent, SWT.NONE);
		topLevel.setLayout(layout);
		topLevel.setFont(parent.getFont());
		
		fileChooser = new FileChooser(topLevel);
		
		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(topLevel);
	}
	
	protected File getFile() {
		return fileChooser.getFile();
	}
}
