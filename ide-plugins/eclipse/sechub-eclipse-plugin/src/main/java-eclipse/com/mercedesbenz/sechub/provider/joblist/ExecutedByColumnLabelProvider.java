// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.provider.joblist;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubJobInfoForUser;

public class ExecutedByColumnLabelProvider extends ColumnLabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof SecHubJobInfoForUser info) {
			return info.getExecutedBy();
		}
		return super.getText(element);
	}

}