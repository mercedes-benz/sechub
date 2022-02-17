// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.job;

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
		id=UseCaseIdentifier.UC_ADMIN_CANCELS_JOB,
		group=UseCaseGroup.JOB_ADMINISTRATION,
		apiName="adminCancelsJob",
		title="Admin cancels a job",
		description="Administrator does cancel a job by its Job UUID")
public @interface UseCaseAdminCancelsJob {

	Step value();
}
/* @formatter:on */
