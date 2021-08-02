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
		id=UseCaseIdentifier.UC_USER_UNMARKS_FALSE_POSITIVES,
		group=UseCaseGroup.SECHUB_EXECUTION,
		apiName="userUnmarksFalsePositives",
		title="User unmarks existing false positive definitons", 
		description="user/unmark_false_positives.adoc")
public @interface UseCaseUserUnmarksFalsePositives {
	
	Step value();
}
/* @formatter:on */
