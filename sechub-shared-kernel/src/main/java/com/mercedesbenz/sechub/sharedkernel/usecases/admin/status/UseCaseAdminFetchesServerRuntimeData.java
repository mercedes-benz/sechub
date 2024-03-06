// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.admin.status;

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
		id=UseCaseIdentifier.UC_ADMIN_FETCHES_SERVER_RUNTIME_DATA,
		group=UseCaseGroup.OTHER,
		apiName="adminFetchesServerRuntimeData",
		title="Admin fetches server runtime data",
		description="An administrator fetches the current SecHub server runtime data. Only administrators are allowed to do this because it contains the server version and knowing the exact server version makes it easier for penetration tester or attacker to attack the system.")
public @interface UseCaseAdminFetchesServerRuntimeData {

	Step value();
}
/* @formatter:on */