// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

import com.mercedesbenz.sechub.server.data.SecHubServerDataModel.SecHubServerConnection;
import com.mercedesbenz.sechub.util.EclipseUtil;

public class SechubServerTreeDecorator implements ILightweightLabelDecorator {

	private static ImageDescriptor DESC_OVERLAY_OK = EclipseUtil.createImageDescriptor("/icons/ovr/ok_ovr.png");
	private static ImageDescriptor DESC_OVERLAY_ERROR = EclipseUtil.createImageDescriptor("/icons/ovr/error_ovr.png");

	@Override
	public void decorate(Object element, IDecoration decoration) {

		if (element instanceof SecHubServerConnection) {
			SecHubServerConnection server = (SecHubServerConnection) element;
			ImageDescriptor descriptor;
			if (server.isAlive()) {
				descriptor = DESC_OVERLAY_OK;
			} else { 
				descriptor = DESC_OVERLAY_ERROR;
			}
			decoration.addOverlay(descriptor, IDecoration.BOTTOM_RIGHT);
		}

	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		
	}

	@Override
	public void dispose() {
		
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		
	}

}
