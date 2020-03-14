// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.usecases.job;

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
		id=UseCaseIdentifier.UC_ADMIN_RESTARTS_JOB_HARD,
		group=UseCaseGroup.JOB_ADMINISTRATION,
		title="Admin restarts a job (hard)",
		description="Administrator restarts job. Existing product results will be dropped and products restarted. Job UUID will be same")
public @interface UseCaseAdministratorRestartsJobHard {

	Step value();
}
/* @formatter:on */
