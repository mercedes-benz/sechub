// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.mercedesbenz.sechub.util.EclipseUtil;

public class RefreshSecHubServerViewAction extends Action {
	
	private SecHubServerView view;
	
	private static ImageDescriptor IMAGE_DESCRIPTOR = EclipseUtil.createImageDescriptor("/icons/refresh.png");

	public RefreshSecHubServerViewAction(SecHubServerView view) {
		this.view=view;
		
		 setImageDescriptor(IMAGE_DESCRIPTOR);
         setText("Refresh");
         setToolTipText("Refresh server view");
	}
	
	@Override
	public void run() {
		view.refreshServerView();
	}
}
