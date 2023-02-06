// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.job;

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
		id=UseCaseIdentifier.UC_USER_LISTS_JOBS_FOR_PROJECT,
		group=UseCaseGroup.OTHER,
		apiName="userListsJobsforProject",
		title="User lists jobs for project",
		description="job/user_lists_jobs_for_project.adoc")
public @interface UseCaseUserListsJobsForProject {

	Step value();
}
/* @formatter:on */
