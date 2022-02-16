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
		id=PDSUseCaseIdentifier.UC_USER_MARKS_JOB_READY_TO_START,
		group=PDSUseCaseGroup.JOB_EXECUTION,
		title="User marks job ready to start",
		description="A user marks an existing PDS job as ready to start. Means all intermediate parts are done - e.g. uploads")
public @interface UseCaseUserMarksJobReadyToStart {
    PDSStep value();
}
/* @formatter:on */
