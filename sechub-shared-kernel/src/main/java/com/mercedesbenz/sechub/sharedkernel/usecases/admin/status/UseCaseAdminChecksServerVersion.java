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
		id=UseCaseIdentifier.UC_ADMIN_CHECKS_SERVER_VERSION,
		group=UseCaseGroup.OTHER,
		apiName="adminChecksServerVersion",
		title="Admin checks server version",
		description="An administrator checks the current SecHub server version. Only administrators are able to check the server version, because knowing the exact server version makes it easier for penetration tester or attacker to attack the system.")
public @interface UseCaseAdminChecksServerVersion {

	Step value();
}
/* @formatter:on */