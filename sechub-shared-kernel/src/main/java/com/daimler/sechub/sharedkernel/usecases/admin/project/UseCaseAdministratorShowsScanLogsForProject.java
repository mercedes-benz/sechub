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
		id=UseCaseIdentifier.UC_USER_SHOWS_PROJECT_SCAN_INFO,
		group=UseCaseGroup.PROJECT_ADMINISTRATION,
		apiName="administratorShowsScanLogsForProject",
		title="Admin shows scan logs for project",
		description="An admin downloads a json file containing log for scans of project"
					)
public @interface UseCaseAdministratorShowsScanLogsForProject {

	Step value();
}
/* @formatter:on */
