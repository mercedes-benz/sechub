// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.provider;

import org.eclipse.jface.viewers.ITreeContentProvider;

import com.mercedesbenz.sechub.model.FindingModel;
import com.mercedesbenz.sechub.model.FindingNode;

public class FindingModelTreeContentProvider implements ITreeContentProvider {
	private static final Object[] NONE = new Object[] {};
	
	@Override
	public Object[] getElements(Object inputElement) {
		if (! (inputElement instanceof FindingModel)) {
			return NONE;
		}
		FindingModel model = (FindingModel) inputElement;
		return model.getFindings().toArray();
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (! (parentElement instanceof FindingNode)) {
			return NONE;
		}
		FindingNode parentNode = (FindingNode) parentElement;
		return parentNode.getChildren().toArray();
	}

	@Override
	public Object getParent(Object element) {
		if (! (element instanceof FindingModel)) {
			return null;
		}
		FindingNode node = (FindingNode) element;
		return node.getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof FindingNode) {
			FindingNode parent = (FindingNode) element;
			return parent.hasChildren();
		}
		return false;
	}
}
