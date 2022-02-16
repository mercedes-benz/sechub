// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.admin.project;

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
		id=UseCaseIdentifier.UC_ADMIN_DELETES_PROJECT,
		group=UseCaseGroup.PROJECT_ADMINISTRATION,
		apiName="adminDeleteProject",
		title="Admin deletes a project",
		description="project/admin_deletes_project.adoc")
public @interface UseCaseAdminDeleteProject {

	Step value();
}
/* @formatter:on */
