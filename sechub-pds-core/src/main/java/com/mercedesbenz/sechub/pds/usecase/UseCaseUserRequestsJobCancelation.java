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
		id=PDSUseCaseIdentifier.UC_USER_REQUESTS_JOB_CANCELATION,
		group=PDSUseCaseGroup.JOB_EXECUTION,
		title="User cancels job",
		description="A user requests to cancel a PDS job")
public @interface UseCaseUserRequestsJobCancelation {
    PDSStep value();
}
/* @formatter:on */
