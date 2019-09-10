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
		id=UseCaseIdentifier.UC_ADMIN_STARTS_SCHEDULER,
		group=UseCaseGroup.OTHER,
		title="Admin starts scheduling",
		description="An administrator starts scheduler. This can be a necessary step after for system wide update - when scheduling was stoped before.")
public @interface UseCaseAdministratorStartScheduler {

	Step value();
}
/* @formatter:on */
