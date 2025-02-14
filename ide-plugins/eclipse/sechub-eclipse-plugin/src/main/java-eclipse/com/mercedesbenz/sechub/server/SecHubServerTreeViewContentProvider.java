// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;

import com.mercedesbenz.sechub.server.data.SecHubServerDataModel;
import com.mercedesbenz.sechub.server.data.SecHubServerDataModel.ServerElement;

public class SecHubServerTreeViewContentProvider implements ITreeContentProvider {

	private static Object[] NO_CONTENT = new Object[] {};

	@Override
	public Object[] getElements(Object inputElement) {
		
		if (inputElement instanceof SecHubServerDataModel) {
			List<ServerElement> servers = ((SecHubServerDataModel) inputElement).getServers();
			if (servers.isEmpty()) {
				return new String[] { "No server defined" };
			}
			return servers.toArray();
		}
		return NO_CONTENT;

	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return NO_CONTENT;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof ServerElement) {
			return ((ServerElement) element).getModel();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof SecHubServerDataModel) {
			return !((SecHubServerDataModel) element).getServers().isEmpty();
		}
		return false;
	}

}
