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
        id=PDSUseCaseIdentifier.UC_ADMIN_UPDATES_AUTO_CLEANUP_CONFIGURATION,
        group=PDSUseCaseGroup.AUTO_CLEANUP,
        title="Admin updates auto cleanup configuration",
        description="An administrator can update the auto cleanup configuration via REST.")
public @interface UseCaseAdminUpdatesAutoCleanupConfiguration {
    PDSStep value();
}
/* @formatter:on */
