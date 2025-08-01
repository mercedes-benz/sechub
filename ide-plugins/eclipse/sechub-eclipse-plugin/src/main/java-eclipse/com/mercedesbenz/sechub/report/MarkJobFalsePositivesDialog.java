// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.report;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class MarkJobFalsePositivesDialog extends Dialog {

	private StyledText additionalDescription;
	private int amountOfFindings;
	private Button[] radioButtons;
	private String comment = "";

	public MarkJobFalsePositivesDialog(Shell parentShell, int amountOfFindings) {
		super(parentShell);
		this.amountOfFindings = amountOfFindings;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Mark selected " + amountOfFindings + " finding(s) as false positives");
		newShell.setSize(400, 300);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));

		Composite group = container;
		radioButtons = new Button[] { createRadioButton("A fix has already been started", group),
				createRadioButton("No bandwidth to fix this", group),
				createRadioButton("Risk is tolerable to this project", group),
				createRadioButton("This alert is inaccurate or incorrect", group),
				createRadioButton("Vulnerable code is not actually used", group) };

		Label spacer = new Label(container, SWT.NONE);
		spacer.setText("");

		Label additionalDescriptionLabel = new Label(container, SWT.NONE);
		additionalDescriptionLabel.setText("Additional description:");

		additionalDescription = new StyledText(container, SWT.WRAP | SWT.BORDER);
		GridData messagesLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		additionalDescription.setLayoutData(messagesLayoutData);

		radioButtons[0].setSelection(true);

		return container;
	}

	private Button createRadioButton(String text, Composite group) {
		Button radio0 = new Button(group, SWT.RADIO);
		radio0.setText(text);
		return radio0;
	}

	@Override
	protected void okPressed() {
		StringBuilder sb = new StringBuilder();

		for (Button radioButton : radioButtons) {
			if (radioButton.getSelection()) {
				sb.append(radioButton.getText());
			}
		}
		String adiditionalDescriptionText = additionalDescription.getText();
		if (adiditionalDescriptionText!=null && !adiditionalDescriptionText.isBlank()) {
			sb.append(", ");
			sb.append(adiditionalDescriptionText);
		}

		comment = sb.toString();

		super.okPressed();
	}

	public String getComment() {
		return comment;
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}
}