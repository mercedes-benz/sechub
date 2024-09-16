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
        id=PDSUseCaseIdentifier.UC_SYSTEM_SIGTERM_HANDLING,
        group=PDSUseCaseGroup.OTHER,
        title="System handles SIGTERM",
        description="The PDS does listen to SIGTERM signal from OS and does necessary steps for next restart.")
public @interface UseCaseSystemSigTermHandling {
    PDSStep value();
}
/* @formatter:on */
