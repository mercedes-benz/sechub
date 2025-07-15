// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import org.eclipse.jface.viewers.ITreeContentProvider;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubJobInfoForUserListPage;

public class SecHubJobTreeViewContentProvider implements ITreeContentProvider {

	private static Object[] NO_CONTENT = new Object[] {};

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof SecHubJobInfoForUserListPage page) {
			return page.getContent().toArray();
		}
		return NO_CONTENT;

	}

	@Override
	public Object[] getChildren(Object parentElement) {
//		if (parentElement instanceof SecHubJobInfoForUser infoForUser) {
//			return new Object[] {infoForUser.getCreated(), infoForUser.getTrafficLight(), infoForUser.getExecutedBy(), infoForUser.getJobUUID()};
//		}
		return NO_CONTENT;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof SecHubJobInfoForUserListPage page) {
			return !page.getContent().isEmpty();
		}
		return false;
	}

}
