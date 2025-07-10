// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.provider.joblist;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import com.mercedesbenz.sechub.api.internal.gen.model.ExecutionState;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubJobInfoForUser;

public class StatusColumnLabelProvider extends ColumnLabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof SecHubJobInfoForUser info) {
			ExecutionState executionState = info.getExecutionState();
			if (executionState==null) {
				return null;
			}
			return executionState.getValue();
		}
		return super.getText(element);
	}

}