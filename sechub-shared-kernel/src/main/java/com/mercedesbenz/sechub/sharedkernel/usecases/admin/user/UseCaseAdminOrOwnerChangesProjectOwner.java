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
		id=UseCaseIdentifier.UC_ADMIN_OR_OWNER_CHANGES_PROJECT_OWNER,
		group=UseCaseGroup.USER_ADMINISTRATION,
		apiName="adminOrOwnerChangesProjectOwner",
		title="Admin or owner changes owner of a project",
		description="An administrator or the current project owner changes the ownership of an existing sechub project.")
public @interface UseCaseAdminOrOwnerChangesProjectOwner {

	Step value();
}
/* @formatter:on */
