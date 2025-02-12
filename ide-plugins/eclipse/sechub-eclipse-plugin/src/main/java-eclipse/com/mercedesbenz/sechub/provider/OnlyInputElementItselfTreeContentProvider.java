// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.provider;

import org.eclipse.jface.viewers.ITreeContentProvider;

public class OnlyInputElementItselfTreeContentProvider implements ITreeContentProvider {

	private static final Object[] NONE = new Object[] {};

	@Override
	public Object[] getElements(Object inputElement) {
		return new Object[] { inputElement };
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return NONE;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return false;
	}
}
