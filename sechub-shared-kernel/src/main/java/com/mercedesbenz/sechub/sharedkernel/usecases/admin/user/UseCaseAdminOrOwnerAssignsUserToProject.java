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
		id=UseCaseIdentifier.UC_ADMIN_OR_OWNER_ASSIGNS_USER_TO_PROJECT,
		group=UseCaseGroup.USER_ADMINISTRATION,
		apiName="adminOrOwnerAssignsUserToProject",
		title="Admin or owner assigns user to project",
		description="An administrator or project owner assigns an user to an existing sechub project.")
public @interface UseCaseAdminOrOwnerAssignsUserToProject {

	Step value();
}
/* @formatter:on */
