// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.admin.config;

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
		id=UseCaseIdentifier.UC_ADMIN_UNASSIGNS_EXECUTION_PROFILE_FROM_PROJECT,
		group=UseCaseGroup.CONFIGURATION,
		apiName="adminUnassignsExecutionProfileFromProject",
		title="Admin unassigns execution profile from project",
		description="An administrator unassigns an execution profile from a projects.")
public @interface UseCaseAdminUnassignsExecutionProfileFromProject{

	Step value();
}
/* @formatter:on */
