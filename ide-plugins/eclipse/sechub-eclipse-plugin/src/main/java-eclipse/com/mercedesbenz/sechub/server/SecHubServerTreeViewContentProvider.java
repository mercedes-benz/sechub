// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import org.eclipse.jface.viewers.ITreeContentProvider;

import com.mercedesbenz.sechub.server.data.SecHubServerDataModel;
import com.mercedesbenz.sechub.server.data.SecHubServerDataModel.SecHubServerConnection;

public class SecHubServerTreeViewContentProvider implements ITreeContentProvider {

	private static Object[] NO_CONTENT = new Object[] {};

	@Override
	public Object[] getElements(Object inputElement) {

		if (inputElement instanceof SecHubServerDataModel model) {
			SecHubServerConnection connection = model.getConnection();
			if (connection==null) {
				return new String[] { "Server not defined" };
			}
			return new Object[] {connection};
		}
		return NO_CONTENT;

	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return NO_CONTENT;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof SecHubServerConnection) {
			return ((SecHubServerConnection) element).getModel();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return false;
	}

}
