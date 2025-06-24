// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.usecase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/* @formatter:off */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PDSUseCaseDefinition(
		id=PDSUseCaseIdentifier.UC_ADMIN_FETCHES_SERVER_CONFIGURATION,
		group=PDSUseCaseGroup.MONITORING,
		title="Admin fetches server configuration",
		description="An administrator fetches the server configuration.")
public @interface UseCaseAdminFetchesServerConfiguration {
    PDSStep value();
}
/* @formatter:on */
