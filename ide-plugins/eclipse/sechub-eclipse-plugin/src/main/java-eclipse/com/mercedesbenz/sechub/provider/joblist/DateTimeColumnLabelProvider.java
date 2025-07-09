// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.provider.joblist;

import java.time.OffsetDateTime;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubJobInfoForUser;

public class DateTimeColumnLabelProvider extends ColumnLabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof SecHubJobInfoForUser info) {
			OffsetDateTime created = info.getCreated();
			
			return created.toString();
		}
		return super.getText(element);
	}

}