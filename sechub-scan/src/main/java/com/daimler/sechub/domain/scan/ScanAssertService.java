// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.scan.access.ScanUserAccessToProjectValidationService;
import com.daimler.sechub.domain.scan.report.ScanReport;
import com.daimler.sechub.sharedkernel.UserContextService;

@Service
public class ScanAssertService {

	@Autowired
	UserContextService userContextService;

	@Autowired
	ScanUserAccessToProjectValidationService userAccessValidation;

	public void assertUserHasAccessToReport(ScanReport report) {
		if (report==null) {
			throw new IllegalArgumentException("report may not be null");
		}
		assertUserHasAccessToProject(report.getProjectId());

	}

	public void assertUserHasAccessToProject(String projectId) {
		if (projectId==null) {
			throw new IllegalArgumentException("projectId may not be null");
		}
		if (userContextService.isSuperAdmin()) {
			/* always access */
			return;
		}
		userAccessValidation.assertUserHasAccessToProject(projectId);

	}
}
