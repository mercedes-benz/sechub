// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.component;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * File chooser component allows a user to select a SecHub report from the local
 * file system.
 */
public class FileChooser extends Composite {
	private Text fileInputField;
	private Button button;
	private String title = null;
	private Label label;

	public FileChooser(Composite parent) {
		super(parent, SWT.NULL);
		createContent();
	}

	public void createContent() {
		int numColumns = 3;
		boolean columnsEqualWidth = false;

		GridLayout layout = new GridLayout(numColumns, columnsEqualWidth);
		setLayout(layout);

		label = new Label(this, SWT.LEFT);
		label.setText("File:");

		fileInputField = new Text(this, SWT.SINGLE | SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.minimumWidth = 300;
		fileInputField.setLayoutData(gridData);

		button = new Button(this, SWT.NONE);
		button.setText("Open File...");

		button.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(button.getShell(), SWT.OPEN);
				fileDialog.setText("Open");
				String path = fileDialog.open();

				if (path != null) {
					fileInputField.setText(path);
				}
			}
		});
	}

	public String getText() {
		return fileInputField.getText();
	}

	public Text getTextControl() {
		return fileInputField;
	}

	public File getFile() {
		File file = null;

		String path = fileInputField.getText();

		if (path.length() > 0) {
			file = new File(path);
		}

		return file;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
