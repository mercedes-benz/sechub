// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.mercedesbenz.sechub.util.EclipseUtil;

public class SearchJobDirectlyServerViewAction extends Action {

	private SecHubServerView view;

	private static ImageDescriptor IMAGE_DESCRIPTOR = EclipseUtil.createImageDescriptor("/icons/search.png");

	public SearchJobDirectlyServerViewAction(SecHubServerView view) {
		this.view = view;

		setImageDescriptor(IMAGE_DESCRIPTOR);
		setText("Search job");
		setToolTipText("Search job directly");
	}

	@Override
	public void run() {
		view.searchJobDirectly();
	}
	
}
