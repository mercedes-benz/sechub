// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.usecases.admin.user;

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
		id=UseCaseIdentifier.UC_ADMIN_REVOKES_ADMIN_RIGHTS_FROM_ANOTHER_ADMIN,
		group=UseCaseGroup.USER_ADMINISTRATION,
		title="Admin revokes admin rights from an admin",
		description="An administrator revokes existing admin rights from another administrator.")
public @interface UseCaseAdministratorRevokesAdminRightsFromAdmin {

	Step value();
}
/* @formatter:on */
