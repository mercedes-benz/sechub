// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.provider.joblist;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubJobInfoForUser;

public class DateTimeColumnLabelProvider extends ColumnLabelProvider {
	
	private final static DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Override
	public String getText(Object element) {
		if (element instanceof SecHubJobInfoForUser info) {
			OffsetDateTime created = info.getCreated();
			if (created==null) {
				return null;
			}
			return fmt.format(created);
		}
		return super.getText(element);
	}

}