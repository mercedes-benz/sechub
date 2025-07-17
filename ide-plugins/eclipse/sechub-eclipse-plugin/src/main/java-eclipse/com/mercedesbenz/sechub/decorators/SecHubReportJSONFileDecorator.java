// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.decorators;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

import com.mercedesbenz.sechub.util.EclipseUtil;

public class SecHubReportJSONFileDecorator implements ILightweightLabelDecorator {

	/**
	 * The image description used in <code>addOverlay(ImageDescriptor, int)</code>
	 */
	private ImageDescriptor descriptor;

	public void decorate(Object element, IDecoration decoration) {
		if (! (element instanceof IResource)) {
			return;
		}
		IResource resource = (IResource) element;
		if (descriptor == null) {
			descriptor = EclipseUtil.createDescriptor("icons/sechub-decorator.gif");
		}
		// default file names are sechub_report_${JOB_UID}.json - for example: "sechub_report_004f006d-1b8f-4ff7-a9a8-cabd1ec615ed.json"
		if (resource.getName().startsWith("sechub_report_") && resource.getFileExtension().contentEquals("json")){
			decoration.addOverlay(descriptor, IDecoration.BOTTOM_RIGHT);
		}
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}
}