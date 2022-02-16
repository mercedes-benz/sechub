// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.user.execute;

import static com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseIdentifier.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseDefinition;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseGroup;

/* @formatter:off */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@UseCaseDefinition(
		id=UC_USER_CREATES_JOB,
		group=UseCaseGroup.SECHUB_EXECUTION,
		apiName="userCreatesNewJob",
		title="User creates a new sechub job",
		description="user/create_sechub_job_description.adoc")
public @interface UseCaseUserCreatesNewJob {

	Step value();
}
/* @formatter:on */
