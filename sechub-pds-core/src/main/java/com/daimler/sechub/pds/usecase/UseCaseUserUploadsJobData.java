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
		id=PDSUseCaseIdentifier.UC_USER_UPLOADS_JOB_DATA,
		group=PDSUseCaseGroup.JOB_EXECUTION,
		title="User uploads job data",
		description="A user uploads data for a job")
public @interface UseCaseUserUploadsJobData {
    PDSStep value();
}
/* @formatter:on */
