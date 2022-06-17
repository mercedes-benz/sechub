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
        id=PDSUseCaseIdentifier.UC_SYSTEM_AUTO_CLEANUP_EXECUTION,
        group=PDSUseCaseGroup.AUTO_CLEANUP,
        title="System executes auto cleanup",
        description="The PDS does execute an auto cleanup operation.")
public @interface UseCaseSystemExecutesAutoCleanup {
    PDSStep value();
}
/* @formatter:on */
