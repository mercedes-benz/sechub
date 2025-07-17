// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.mercedesbenz.sechub.util.EclipseUtil;

public class PreviousJobPageSecHubServerViewAction extends Action {

	private SecHubServerView view;

	private static ImageDescriptor IMAGE_DESCRIPTOR = EclipseUtil.createImageDescriptor("/icons/arrow-left.png");

	public PreviousJobPageSecHubServerViewAction(SecHubServerView view) {
		this.view = view;

		setImageDescriptor(IMAGE_DESCRIPTOR);
		setText("Previous");
		setToolTipText("Load previous page");
	}

	@Override
	public void run() {
		view.previousPage();
	}
	
}
