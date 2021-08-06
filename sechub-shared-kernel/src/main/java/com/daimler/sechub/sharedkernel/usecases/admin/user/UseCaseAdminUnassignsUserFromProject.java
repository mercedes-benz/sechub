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
/* @formatter:on */
@Retention(RetentionPolicy.RUNTIME)
@UseCaseDefinition(
		id=UseCaseIdentifier.UC_ADMIN_UNASSIGNS_USER_FROM_PROJECT,
		group= {UseCaseGroup.USER_ADMINISTRATION,UseCaseGroup.PROJECT_ADMINISTRATION},
		apiName="adminUnassignsUserFromProject",
		title="Admin unassigns user from project", 
		description="An administrator unassigns an user from a sechub project.")
public @interface UseCaseAdminUnassignsUserFromProject {
	
	Step value();
}
