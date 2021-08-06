// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.usecases.admin.schedule;

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
		id=UseCaseIdentifier.UC_ADMIN_ENABLES_SCHEDULER_JOB_PROCESSING,
		group=UseCaseGroup.OTHER,
		apiName="adminEnablesSchedulerJobProcessing",
		title="Admin enables scheduler job processing",
		description="An administrator starts scheduler job processing. This can be a necessary step after a system wide update where processing of jobs was stoped before.")
public @interface UseCaseAdminEnablesSchedulerJobProcessing {

	Step value();
}
/* @formatter:on */
