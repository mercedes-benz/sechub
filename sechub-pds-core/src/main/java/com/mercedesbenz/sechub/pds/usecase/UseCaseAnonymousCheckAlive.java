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
            id=PDSUseCaseIdentifier.UC_ANONYMOUS_CHECK_ALIVE,
            group=PDSUseCaseGroup.ANONYMOUS,
            title="Anonymous check if server is alive",
            description="Anonymous access to check if server is alive or not")
    public @interface UseCaseAnonymousCheckAlive {
        PDSStep value();
}
