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
		id=UseCaseIdentifier.UC_UPDATE_PROJECT_WHITELIST,
		group=UseCaseGroup.PROJECT_ADMINISTRATION,
		apiName="updateProjectWhitelist",
		title="Update project whitelist",
		description="project/admin_creates_project.adoc")
public @interface UseCaseUpdateProjectWhitelist {

	Step value();
}
/* @formatter:on */
