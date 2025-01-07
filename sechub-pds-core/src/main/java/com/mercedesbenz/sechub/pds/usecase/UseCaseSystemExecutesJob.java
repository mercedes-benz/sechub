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
        id=PDSUseCaseIdentifier.UC_SYSTEM_JOB_EXECUTION,
        group=PDSUseCaseGroup.JOB_EXECUTION,
        title="System executes job",
        description="The PDS does execute a PDS job.")
public @interface UseCaseSystemExecutesJob {
    PDSStep value();
}
