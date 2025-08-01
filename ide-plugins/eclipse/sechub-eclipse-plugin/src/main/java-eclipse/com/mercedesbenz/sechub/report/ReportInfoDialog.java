// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.report;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubMessage;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubReport;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubReportMetaData;

public class ReportInfoDialog extends Dialog {

	private StyledText messagesText;
	private SecHubReport report;

	public ReportInfoDialog(Shell parentShell, SecHubReport report) {
		super(parentShell);
		this.report = report;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Information about job: " + report.getJobUUID());
		newShell.setSize(400, 300);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// Only create the OK button
		createButton(parent, Dialog.OK, "OK", true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));

		Label messagesLabel = new Label(container, SWT.NONE);
		messagesLabel.setText("Messages:");

		List<SecHubMessage> messages = report.getMessages();
		StringBuilder sb = new StringBuilder();

		for (SecHubMessage message : messages) {
			sb.append(message.getType()).append(':').append(message.getText()).append("\n");
		}

		messagesText = new StyledText(container, SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.READ_ONLY);
		messagesText.setAlwaysShowScrollBars(false);
		GridData messagesLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		messagesText.setLayoutData(messagesLayoutData);
		messagesText.setText(sb.toString());

		Label labelsLabel = new Label(container, SWT.NONE);

		labelsLabel.setText("Labels:");

		SecHubReportMetaData metaData = report.getMetaData();
		if (metaData != null && metaData.getLabels() != null && !metaData.getLabels().isEmpty()) {
			for (Map.Entry<String, Object> entry : metaData.getLabels().entrySet()) {
				Label label = new Label(container, SWT.NONE);
				GridData labelLayoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
				label.setLayoutData(labelLayoutData);
				label.setText(entry.getKey() + "=" + Objects.toString(entry.getValue()));
			}

		} else {
			Label label = new Label(container, SWT.NONE);
			GridData labelLayoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
			label.setLayoutData(labelLayoutData);
			label.setText("(No labels defined)");
		}

		return container;
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}
}