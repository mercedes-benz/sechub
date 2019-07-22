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
		id=UseCaseIdentifier.UC_ADMIN_LISTS_ALL_ADMINS,
		group=UseCaseGroup.USER_ADMINISTRATION,
		title="Admin lists all admins",
		description="An administrator downloads a json file containing all names of sechub admins")
public @interface UseCaseAdministratorListsAllAdmins {

	Step value();
}
/* @formatter:on */
