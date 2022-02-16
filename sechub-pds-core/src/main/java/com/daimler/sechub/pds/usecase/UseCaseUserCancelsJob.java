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
		id=PDSUseCaseIdentifier.UC_USER_CANCELS_JOB,
		group=PDSUseCaseGroup.JOB_EXECUTION,
		title="User cancels job",
		description="A user cancel an existing PDS job")
public @interface UseCaseUserCancelsJob {
    PDSStep value();
}
/* @formatter:on */
