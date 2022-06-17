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
        id=PDSUseCaseIdentifier.UC_ADMIN_FETCHES_AUTO_CLEANUP_CONFIGURATION,
        group=PDSUseCaseGroup.AUTO_CLEANUP,
        title="Admin fetches auto cleanup configuration",
        description="An administrator can fetch the auto cleanup configuration via REST.")
public @interface UseCaseAdminFetchesAutoCleanupConfiguration {
    PDSStep value();
}
/* @formatter:on */
