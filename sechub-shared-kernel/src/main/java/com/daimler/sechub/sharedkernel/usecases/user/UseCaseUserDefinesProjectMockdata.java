// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.usecases.user;

import static com.daimler.sechub.sharedkernel.usecases.UseCaseIdentifier.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.UseCaseDefinition;
/* @formatter:off */
import com.daimler.sechub.sharedkernel.usecases.UseCaseGroup;
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@UseCaseDefinition(
		id=UC_USER_DEFINES_PROJECT_MOCKDATA_CONFIGURATION,
		group=UseCaseGroup.TESTING,
		title="User defines mock data configuration for project", 
		description="user/defines_mockdata_for_project.adoc")
public @interface UseCaseUserDefinesProjectMockdata {
	
	Step value();
}
/* @formatter:on */
