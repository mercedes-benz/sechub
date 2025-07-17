package com.mercedesbenz.sechub.server;

import java.util.UUID;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class JobUUIDDialog extends Dialog {

	private UUID jobUUID;
	private Text jobUUIDText;
	private String projectId;

	public JobUUIDDialog(Shell parentShell, String projectId) {
		super(parentShell);
		this.projectId = projectId;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Load SecHub job for project '" + projectId + "'");
		newShell.setSize(400, 100);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));

		Label label = new Label(container, SWT.NONE);
		label.setText("Job UUID:");

		jobUUIDText = new Text(container, SWT.BORDER);
		jobUUIDText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return container;
	}

	@Override
	protected void okPressed() {
		jobUUID = null; // at least reset last job uuid
		try {
			String jobUUIDasText = jobUUIDText.getText();
			jobUUID = UUID.fromString(jobUUIDasText);

			super.okPressed();
		} catch (IllegalArgumentException e) {
			MessageDialog.openError(getShell(), "Invalid UUID", "The entered string is not a valid UUID.");
		}
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}

	public UUID getJobUUID() {
		return jobUUID;
	}
}