// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.usecases.user.execute;

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
		id=UseCaseIdentifier.UC_USER_MARKS_FALSE_POSITIVES_FOR_FINISHED_JOB,
		group=UseCaseGroup.SECHUB_EXECUTION,
		apiName="userMarksFalsePositivesForJob",
		title="User marks false positives for finished sechub job", 
		description="user/mark_false_positives_for_job.adoc")
public @interface UseCaseUserMarksFalsePositivesForJob {
	
	Step value();
}
/* @formatter:on */
