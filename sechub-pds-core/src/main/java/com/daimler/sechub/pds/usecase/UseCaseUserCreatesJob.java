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
		id=PDSUseCaseIdentifier.UC_USER_CREATES_JOB,
		group=PDSUseCaseGroup.JOB_EXECUTION,
		title="User creates job",
		description="A user creates a new PDS job")
public @interface UseCaseUserCreatesJob {
    PDSStep value();
}
/* @formatter:on */
