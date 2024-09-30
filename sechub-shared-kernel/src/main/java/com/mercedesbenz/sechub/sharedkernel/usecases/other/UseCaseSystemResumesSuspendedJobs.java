// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.other;

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
		id=UseCaseIdentifier.UC_SYSTEM_RESUMES_FORMER_SUSPENDED_JOBS,
		group=UseCaseGroup.OTHER,
		apiName="systemResumesSuspendedJobs",
		title="System resumes suspended jobs",
		description="""
		        SecHub jobs which have been suspended a minimum duration time
		        will be restarted automatically.
		        """)
public @interface UseCaseSystemResumesSuspendedJobs {

	Step value();
}
/* @formatter:on */
