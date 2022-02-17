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
		id=UseCaseIdentifier.UC_ADMIN_REVOKES_ADMIN_RIGHTS_FROM_ANOTHER_ADMIN,
		group=UseCaseGroup.USER_ADMINISTRATION,
		apiName="adminRevokesAdminRightsFromAdmin",
		title="Admin revokes admin rights from an admin",
		description="An administrator revokes existing admin rights from another administrator.")
public @interface UseCaseAdminRevokesAdminRightsFromAdmin {

	Step value();
}
/* @formatter:on */
