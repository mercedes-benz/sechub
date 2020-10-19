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
		id=UseCaseIdentifier.UC_ADMIN_DELETES_EXECUTOR_CONFIGURATION,
		group=UseCaseGroup.CONFIGURATION,
		title="Admin deletes executor configuration",
		description="An administrator deletes an executor by removing the configuration entry identified by its uuid")
public @interface UseCaseAdministratorDeletesExecutorConfiguration{

	Step value();
}
/* @formatter:on */
