// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.component;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * The server chooser component allows the user to load a SecHub report from a server.
 *
 * TODO: This component is currently not used.
 */
public class ServerChooser extends Composite {
	private Label label;

	public ServerChooser(Composite parent) {
		super(parent, SWT.NULL);
		createContent();
	}
	
	public void createContent() {
		int numColumns = 3;
		boolean columnsEqualWidth = false;
		
		GridLayout layout = new GridLayout(numColumns, columnsEqualWidth);
		setLayout(layout);
		
		label = new Label(this, SWT.LEFT);
		label.setText("Server:");
	}
}
