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
		id = UseCaseIdentifier.UC_ADMIN_UPDATES_PROJECT_METADATA,
		group = UseCaseGroup.PROJECT_ADMINISTRATION,
		apiName="updateProjectMetaData",
		title = "Update project metadata",
		description = "project/update_project_metadata.adoc")
public @interface UseCaseUpdateProjectMetaData {

	Step value();
}
/* @formatter:on */
