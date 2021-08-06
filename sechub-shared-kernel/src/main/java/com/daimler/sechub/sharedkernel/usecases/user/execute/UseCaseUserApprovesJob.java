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
		id=UseCaseIdentifier.UC_USER_APPROVES_JOB,
		group=UseCaseGroup.SECHUB_EXECUTION,
		apiName="userApprovesJob",
		title="User approves sechub job", 
		description="user/approve_sechub_job_description.adoc")
public @interface UseCaseUserApprovesJob {
	
	Step value();
}
/* @formatter:on */
