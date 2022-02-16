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

@Target(ElementType.METHOD)
/* @formatter:off */
@Retention(RetentionPolicy.RUNTIME)
@UseCaseDefinition(
		id=UseCaseIdentifier.UC_ADMIN_DELETES_USER,
		group=UseCaseGroup.USER_ADMINISTRATION,
		apiName="adminDeletesUser",
		title="Admin deletes a user",
		description="admin/deleteUser.adoc")
public @interface UseCaseAdminDeletesUser {

	Step value();
}
/* @formatter:on */
