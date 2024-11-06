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
        id=UseCaseIdentifier.UC_GET_PROJECTS,
        group=UseCaseGroup.PROJECT_ADMINISTRATION,
        apiName="getProjects",
        title="get projects",
        description="project/get_projects.adoc")
public @interface UseCaseGetProjects {

    Step value();
}
/* @formatter:on */
