// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;

import com.mercedesbenz.sechub.util.EclipseUtil;

public class OpenProjectFalsePositivesDialogAction extends Action {

	private static ImageDescriptor IMAGE_DESCRIPTOR = EclipseUtil.createImageDescriptor("/icons/false_positives_project_overview.png");

	public OpenProjectFalsePositivesDialogAction() {
		setImageDescriptor(IMAGE_DESCRIPTOR);
		setText("Open project false positives");
		setToolTipText("Open project false positives dialog");
	}

	@Override
	public void run() {
		if (! SecHubServerContext.INSTANCE.isConnectedWithServer()) {
			MessageDialog.openInformation(EclipseUtil.getActiveWorkbenchShell(), "Cannot show project false positives", "You have to connect with a SecHub server to show this dialog");
			return;
		}
		ProjectFalsePositivesDialog dialog = new ProjectFalsePositivesDialog(EclipseUtil.getActiveWorkbenchShell());
		dialog.open();
	}

}
