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
		id=PDSUseCaseIdentifier.UC_USER_FETCHES_JOB_MESSAGES,
		group=PDSUseCaseGroup.JOB_EXECUTION,
		title="User fetches job messages",
		description="A user fetches the messages a product has sent back to user at the end of a job.")
public @interface UseCaseUserFetchesJobMessages {
    PDSStep value();
}
/* @formatter:on */
