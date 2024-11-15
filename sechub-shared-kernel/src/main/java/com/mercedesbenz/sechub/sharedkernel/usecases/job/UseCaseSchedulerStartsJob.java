// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.job;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.security.APIConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseDefinition;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseGroup;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseIdentifier;

/* @formatter:off */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@UseCaseDefinition(
		id=UseCaseIdentifier.UC_SCHEDULER_STARTS_JOB,
		group=UseCaseGroup.TECHNICAL,
		apiName=APIConstants.NO_API_AVAILABLE,
		title="Sechub scheduler starts job",
		description="job/scheduler_starts_job.adoc")
public @interface UseCaseSchedulerStartsJob {

	Step value();
}
/* @formatter:on */
