// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.project;

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
        id=UseCaseIdentifier.UC_GET_PROJECT_DATA,
        group=UseCaseGroup.PROJECT_ADMINISTRATION,
        apiName="getAssignedProjectDataList",
        title="get assigned project data",
        description="project/get_project_data.adoc")
public @interface UseCaseGetAssignedProjectDataList {

    Step value();
}
/* @formatter:on */
