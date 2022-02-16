// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.admin.user;

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
		id=UseCaseIdentifier.UC_ADMIN_GRANTS_ADMIN_RIGHT_TO_ANOTHER_USER,
		group=UseCaseGroup.USER_ADMINISTRATION,
		apiName="adminGrantsAdminRightsToUser",
		title="Admin grants admin rights to user",
		description="An administrator grants admin rights to another user. So this user will become also an administrator.")
public @interface UseCaseAdminGrantsAdminRightsToUser {

	Step value();
}
/* @formatter:on */
