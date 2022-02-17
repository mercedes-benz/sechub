// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.admin.project;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseDefinition;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseGroup;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseIdentifier;

/* @formatter:off */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@UseCaseDefinition(
		id=UseCaseIdentifier.UC_ADMIN_DOWNLOADS_FULL_DETAILS_ABOUT_SCAN_JOB,
		group=UseCaseGroup.USER_ADMINISTRATION,
		apiName="adminDownloadsFullScanDataForJob",
		title="Admin downloads all details about a scan job",
		description="An administrator downloads a ZIP file containing full details of a scan. "+
		"Main reason for this use case is for debugging when there are problems with security products. "+
		"Another reason is for developers to adopt new security products easier."
				    )
public @interface UseCaseAdminDownloadsFullScanDataForJob {

	Step value();
}
/* @formatter:on */
