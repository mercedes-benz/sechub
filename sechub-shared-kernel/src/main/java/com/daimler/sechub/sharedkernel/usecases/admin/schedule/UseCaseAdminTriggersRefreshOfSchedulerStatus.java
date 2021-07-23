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
		id=UseCaseIdentifier.UC_ADMIN_TRIGGERS_REFRESH_SCHEDULER_STATUS,
		group=UseCaseGroup.OTHER,
		apiName="adminTriggersRefreshOfSchedulerStatus",
		title="Admin get scheduler status",
		description="An administrator wants to update information about scheduler status")
public @interface UseCaseAdminTriggersRefreshOfSchedulerStatus {

	Step value();
}
/* @formatter:on */
