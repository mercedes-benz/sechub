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
		id=UseCaseIdentifier.UC_ADMIN_UPDATES_EXECUTOR_CONFIGURATION,
		group=UseCaseGroup.CONFIGURATION,
		apiName="adminUpdatesExecutorConfig",
		title="Admin updates executor configuration setup",
		description="An administrator updateds dedicated executor configuration. The update does change description, enabled state and also used executors, but Will NOT change any associations between profile and projects.")
public @interface UseCaseAdminUpdatesExecutorConfig{

	Step value();
}
/* @formatter:on */
