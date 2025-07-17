// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.provider.joblist;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import com.mercedesbenz.sechub.api.internal.gen.model.ExecutionResult;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubJobInfoForUser;

public class ResultColumnLabelProvider extends ColumnLabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof SecHubJobInfoForUser info) {
			ExecutionResult executionResult = info.getExecutionResult();
			if (executionResult==null) {
				return null;
			}
			return executionResult.getValue();
		}
		return super.getText(element);
	}

}