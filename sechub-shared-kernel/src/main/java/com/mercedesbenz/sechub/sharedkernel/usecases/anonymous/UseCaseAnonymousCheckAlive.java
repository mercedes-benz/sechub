// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.anonymous;

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
		id=UseCaseIdentifier.UC_ANONYMOUS_CHECK_ALIVE,
		group=UseCaseGroup.ANONYMOUS,
		apiName="anonymousCheckAlive",
		title="Check if the server is alive and running.",
		description="An anonymous user or system wants to know if the server is alive and running.")
public @interface UseCaseAnonymousCheckAlive {

	Step value();
}
/* @formatter:on */