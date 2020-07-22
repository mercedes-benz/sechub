// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.usecases.admin.config;

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
		id=UseCaseIdentifier.UC_ADMIN_ADDS_EXECUTOR_CONFIGURATION,
		group=UseCaseGroup.CONFIGURATION,
		title="Admin adds executor configuration",
		description="An administrator adds an executor by adding a new configuration entry. "+
		"The user can definess product identifier and versions of executors. Some executors can be added multiple times (e.g. PDS executors)")
public @interface UseCaseAdministratorAddsExecutorConfiguration{

	Step value();
}
/* @formatter:on */
