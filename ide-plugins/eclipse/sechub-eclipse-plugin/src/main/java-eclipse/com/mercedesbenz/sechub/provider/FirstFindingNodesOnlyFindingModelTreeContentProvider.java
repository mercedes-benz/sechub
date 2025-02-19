// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.provider;

public class FirstFindingNodesOnlyFindingModelTreeContentProvider extends FindingModelTreeContentProvider {

	@Override
	public boolean hasChildren(Object element) {
		/* ignore all children */
		return false;
	}
}
