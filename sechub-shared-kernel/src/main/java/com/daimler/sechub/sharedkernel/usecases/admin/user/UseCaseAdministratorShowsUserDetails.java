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
		id=UseCaseIdentifier.UC_ADMIN_SHOWS_USER_DETAILS,
		group=UseCaseGroup.USER_ADMINISTRATION,
		title="Admin shows user details", 
		description="An administrator downloads a json file containing json containing user details")
public @interface UseCaseAdministratorShowsUserDetails {
	
	Step value();
}
/* @formatter:on */
