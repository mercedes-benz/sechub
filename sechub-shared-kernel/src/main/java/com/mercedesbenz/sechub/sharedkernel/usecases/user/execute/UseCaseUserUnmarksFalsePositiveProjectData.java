// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.user.execute;

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
        id=UseCaseIdentifier.UC_USER_UNMARKS_FALSE_POSITIVES_PROJECT_DATA,
        group=UseCaseGroup.SECHUB_EXECUTION,
        apiName="userUnmarksFalsePositivesProjectData",
        title="User unmarks existing false positive project data definitons",
        description="user/unmark_false_positives_project_data.adoc")
public @interface UseCaseUserUnmarksFalsePositiveProjectData {

    Step value();
}
/* @formatter:on */
