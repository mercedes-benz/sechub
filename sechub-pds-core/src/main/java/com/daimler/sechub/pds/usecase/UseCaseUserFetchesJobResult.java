// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.usecase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/* @formatter:off */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PDSUseCaseDefinition(
		id=PDSUseCaseIdentifier.UC_USER_FETCHES_JOB_RESULT,
		group=PDSUseCaseGroup.JOB_EXECUTION,
		title="User fetches job result", 
		description="A user fetches job result")
public @interface UseCaseUserFetchesJobResult {
    PDSStep value();
}
/* @formatter:on */
