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
		id=UseCaseIdentifier.UC_ADMIN_SHOWS_USER_DETAILS_FOR_EMAIL_ADDRESS,
		group=UseCaseGroup.USER_ADMINISTRATION,
		apiName="adminShowsUserDetailsForEmailAddress",
		title="Admin shows user details for email address",
		description="An administrator fetches user details for an email address.")
public @interface UseCaseAdminShowsUserDetailsForEmailAddress {

	Step value();
}
/* @formatter:on */
