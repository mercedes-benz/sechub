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
		id=UseCaseIdentifier.UC_ADMIN_DISABLES_SCHEDULER_JOB_PROCESSING,
		group=UseCaseGroup.OTHER,
		apiName="adminDisablesSchedulerJobProcessing",
		title="Admin disables job processing in scheduler",
		description="An administrator disables scheduler job processing. This can be a preparation for system wide update - when scheduling is stoped, user can ask for new SecHub Jobs etc. But as long as scheduler is stopped nothing is executed - so JVMs/PODs can be updated in cluster")
public @interface UseCaseAdminDisablesSchedulerJobProcessing {

	Step value();
}
/* @formatter:on */
