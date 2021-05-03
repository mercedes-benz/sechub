// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.usecases.admin.project;

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
		id=UseCaseIdentifier.UC_ADMIN_CHANGES_PROJECT_DESCRIPTION,
		group=UseCaseGroup.PROJECT_ADMINISTRATION,
		title="Admin changes project description", 
		description="An administrator changes project description")
public @interface UseCaseAdministratorChangesProjectDescription {
	
	Step value();
}
/* @formatter:on */
