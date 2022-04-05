// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.admin.user;

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
/* @formatter:on */
@Retention(RetentionPolicy.RUNTIME)
@UseCaseDefinition(id = UseCaseIdentifier.UC_ADMIN_UNASSIGNS_USER_FROM_PROJECT, group = { UseCaseGroup.USER_ADMINISTRATION,
        UseCaseGroup.PROJECT_ADMINISTRATION }, apiName = "adminUnassignsUserFromProject", title = "Admin unassigns user from project", description = "An administrator unassigns an user from a sechub project.")
public @interface UseCaseAdminUnassignsUserFromProject {

    Step value();
}
