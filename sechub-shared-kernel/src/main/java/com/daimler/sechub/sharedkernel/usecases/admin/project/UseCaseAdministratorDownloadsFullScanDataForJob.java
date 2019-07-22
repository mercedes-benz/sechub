// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.usecases.admin.project;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.UseCaseDefinition;
import com.daimler.sechub.sharedkernel.usecases.UseCaseGroup;
import com.daimler.sechub.sharedkernel.usecases.UseCaseIdentifier;

/* @formatter:off */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@UseCaseDefinition(
		id=UseCaseIdentifier.UC_ADMIN_DOWNLOADS_FULL_DETAILS_ABOUT_SCAN_JOB,
		group=UseCaseGroup.USER_ADMINISTRATION,
		title="Admin downloads all details about a scan job",
		description="An administrator downloads a zip file containing full details of a scan. "+
		"Main reason for this use case is for debugging when there are problems with security products."+
		"Another reason is for developers to adopt new security products easier."
				    )
public @interface UseCaseAdministratorDownloadsFullScanDataForJob {

	Step value();
}
/* @formatter:on */
