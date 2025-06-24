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
		id=PDSUseCaseIdentifier.UC_ADMIN_FETCHES_JOB_RESULT,
		group=PDSUseCaseGroup.JOB_EXECUTION,
		title="Admin fetches job result",
		description = """
    		Similar to the usecase when a user is fetching a job result.
    		But will return current job result in any kind of state without throwning an error.
	        """
        )
public @interface UseCaseAdminFetchesJobResult {
    PDSStep value();
}
/* @formatter:on */
