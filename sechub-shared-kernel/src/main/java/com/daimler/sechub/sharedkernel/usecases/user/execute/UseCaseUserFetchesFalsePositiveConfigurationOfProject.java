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
		id=UseCaseIdentifier.UC_USER_FETCHES_FALSE_POSITIVE_CONFIGURATION_OF_PROJECT,
		group=UseCaseGroup.SECHUB_EXECUTION,
		apiName="userFetchesFalsePositiveConfigurationOfProject",
		title="User fetches false positive configuration of project", 
		description="user/fetch_false_positive_configuration_of_project.adoc")
public @interface UseCaseUserFetchesFalsePositiveConfigurationOfProject {
	
	Step value();
}
/* @formatter:on */
