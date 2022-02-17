// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.user;

import static com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseIdentifier.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseDefinition;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseGroup;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@UseCaseDefinition(id = UC_USER_RETRIEVES_PROJECT_MOCKDATA_CONFIGURATION, group = UseCaseGroup.TESTING, apiName = "userRetrievesProjectMockdata", title = "User retrieves mock data configuration for project", description = "user/retrieves_mockdata_for_project.adoc")
public @interface UseCaseUserRetrievesProjectMockdata {

    Step value();
}
/* @formatter:on */
