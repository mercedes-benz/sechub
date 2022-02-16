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
        id=PDSUseCaseIdentifier.UC_ADMIN_FETCHES_OUTPUT_STREAM,
        group=PDSUseCaseGroup.JOB_EXECUTION,
        title="Admin fetches job output stream",
        description="An administrator can fetch the output stream text content "
                + "via REST. Even when the PDS job is still running this is possible")
public @interface UseCaseAdminFetchesJobOutputStream {
    PDSStep value();
}
/* @formatter:on */
