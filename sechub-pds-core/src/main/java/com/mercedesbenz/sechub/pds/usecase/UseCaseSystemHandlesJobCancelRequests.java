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
        id=PDSUseCaseIdentifier.UC_SYSTEM_HANDLES_JOB_CANCEL_REQUESTS,
        group=PDSUseCaseGroup.JOB_EXECUTION,
        title="System handles job cancelation requests",
        description="The PDS does handle job cancel requests. For every PDS job where a cancel request is active and the PDS instance does runs this job, the PDS will cancel it. When a orphaned cancel request is detected, this request will be handled as well.")
public @interface UseCaseSystemHandlesJobCancelRequests {
    PDSStep value();
}
/* @formatter:on */
