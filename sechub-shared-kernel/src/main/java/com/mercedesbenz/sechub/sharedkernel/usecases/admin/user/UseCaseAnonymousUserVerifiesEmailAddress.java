// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.admin.user;

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
		id=UseCaseIdentifier.UC_ANONYMOUS_USER_VERIFIES_EMAIL_ADDRESS,
		group=UseCaseGroup.USER_SELF_SERVICE,
		apiName="anonymousUserVerifiesEmailAddress",
		title="Anonymous user verifies new email address",
		description="The unauthenticated user verifies his new email address by link containing JWT token."
		)
public @interface UseCaseAnonymousUserVerifiesEmailAddress {
	Step value();
}
/* @formatter:on */
