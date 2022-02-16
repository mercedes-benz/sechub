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
        id=PDSUseCaseIdentifier.UC_ADMIN_FETCHES_ERROR_STREAM,
        group=PDSUseCaseGroup.JOB_EXECUTION,
        title="Admin fetches job error stream",
        description="An administrator can fetch the output stream text content "
                + "via REST. Even when the PDS job is still running this is possible")
public @interface UseCaseAdminFetchesJobErrorStream {
    PDSStep value();
}
/* @formatter:on */
