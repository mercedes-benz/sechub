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
@Target(ElementType.METHOD)
/* @formatter:off */
@Retention(RetentionPolicy.RUNTIME)
@UseCaseDefinition(
		id=UseCaseIdentifier.UC_ADMIN_DELETES_SIGNUP, 
		group=UseCaseGroup.SIGN_UP,
		apiName="adminDeletesSignup",
		title="Admin deletes user signup", 
		description="In this usecase the administrator will not accept the self registration done by an user but delete the entry.")
public @interface UseCaseAdminDeletesSignup {
	
	Step value();
}
/* @formatter:on */
