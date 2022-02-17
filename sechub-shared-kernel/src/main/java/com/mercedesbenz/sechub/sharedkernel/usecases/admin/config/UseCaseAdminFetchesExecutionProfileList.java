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
		id=UseCaseIdentifier.UC_ADMIN_FETCHES_EXECUTION_PROFILE_LIST,
		group=UseCaseGroup.CONFIGURATION,
		apiName="adminFetchesExecutionProfileList",
		title="Admin fetches execution profile list",
		description="An administrator fetches execution profile list")
public @interface UseCaseAdminFetchesExecutionProfileList {

	Step value();
}
/* @formatter:on */
