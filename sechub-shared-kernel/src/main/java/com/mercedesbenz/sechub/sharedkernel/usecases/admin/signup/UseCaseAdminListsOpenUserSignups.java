// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.admin.signup;

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
		id=UseCaseIdentifier.UC_ADMIN_LISTS_OPEN_USER_SIGNUPS,
		group=UseCaseGroup.SIGN_UP,
		apiName="adminListsOpenUserSignups",
		title="Admin lists open user signups",
		description="In this usecase the administrator will list the currently unapplied user self registrations/signups.")
public @interface UseCaseAdminListsOpenUserSignups {

	Step value();
}
/* @formatter:on */
