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
		id=UseCaseIdentifier.UC_SYSTEM_SUSPENDS_JOBS_WHEN_SIGTERM_RECEIVED,
		group=UseCaseGroup.OTHER,
		apiName = "systemSigtermSuspendsJobs",
		title="System suspends running jobs on SIGTERM",
		description="""
		        When a SecHub instance is receiving a SIGTERM signal from OS,
		        the server instance must block further job processing (on this instance) and
		        suspend all of it running jobs. Because after some time the job will be resumed
		        by another instance, this process will not stop any running PDS jobs.
		        """)
public @interface UseCaseSystemSuspendsJobsWhenSigTermReceived {

	Step value();
}
/* @formatter:on */
