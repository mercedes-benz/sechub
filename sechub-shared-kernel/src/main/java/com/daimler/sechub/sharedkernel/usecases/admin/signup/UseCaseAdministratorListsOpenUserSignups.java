// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.usecases.admin.signup;

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
		id=UseCaseIdentifier.UC_ADMIN_LISTS_OPEN_USER_SIGNUPS,
		group=UseCaseGroup.SIGN_UP,
		apiName="administratorListsOpenUserSignups",
		title="Admin lists open user signups", 
		description="In this usecase the administrator will list the currently unapplied user self registrations/signups.")
public @interface UseCaseAdministratorListsOpenUserSignups {
	
	Step value();
}
/* @formatter:on */
