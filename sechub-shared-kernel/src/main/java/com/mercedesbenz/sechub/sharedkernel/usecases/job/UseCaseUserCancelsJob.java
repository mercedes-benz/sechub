// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.job;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseDefinition;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseGroup;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseIdentifier;

/* @formatter:off */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@UseCaseDefinition(
        id=UseCaseIdentifier.UC_USER_CANCELS_JOB,
        group=UseCaseGroup.JOB_ADMINISTRATION,
        apiName="userCancelsJob",
        title="User cancels a job",
        description="User does cancel a job by its Job UUID")
public @interface UseCaseUserCancelsJob {
    Step value();
}
/* @formatter:on */
