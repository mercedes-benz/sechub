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
		id=PDSUseCaseIdentifier.UC_USER_FETCHES_JOB_STREAMS,
		group=PDSUseCaseGroup.JOB_EXECUTION,
		title="User fetches job stream text",
		description="A user fetches current job stream text")
public @interface UseCaseUserFetchesJobStream {
    PDSStep value();
}
/* @formatter:on */
