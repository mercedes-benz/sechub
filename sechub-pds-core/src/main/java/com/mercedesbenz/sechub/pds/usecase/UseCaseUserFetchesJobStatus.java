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
		id=PDSUseCaseIdentifier.UC_USER_FETCHES_STATUS_OF_JOB,
		group=PDSUseCaseGroup.JOB_EXECUTION,
		title="User fetches job status",
		description="A user fetches current job status")
public @interface UseCaseUserFetchesJobStatus {
    PDSStep value();
}
/* @formatter:on */
