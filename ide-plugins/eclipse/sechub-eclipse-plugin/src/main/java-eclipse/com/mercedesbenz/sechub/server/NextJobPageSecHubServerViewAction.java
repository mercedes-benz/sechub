// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.mercedesbenz.sechub.util.EclipseUtil;

public class NextJobPageSecHubServerViewAction extends Action {

	private SecHubServerView view;

	private static ImageDescriptor IMAGE_DESCRIPTOR = EclipseUtil.createImageDescriptor("/icons/arrow-right.png");

	public NextJobPageSecHubServerViewAction(SecHubServerView view) {
		this.view = view;

		setImageDescriptor(IMAGE_DESCRIPTOR);
		setText("Next");
		setToolTipText("Load next page");
	}

	@Override
	public void run() {
		view.nextPage();
	}
	
}
