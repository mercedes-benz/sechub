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
		id=UseCaseIdentifier.UC_ADMIN_LISTS_ALL_RUNNING_JOBS,
		group=UseCaseGroup.JOB_ADMINISTRATION,
		apiName="adminListsAllRunningJobs",
		title="Admin lists all running jobs",
		description="job/admin_lists_all_running_jobs.adoc")
public @interface UseCaseAdminListsAllRunningJobs {

	Step value();
}
/* @formatter:on */
