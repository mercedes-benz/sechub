// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.admin.config;

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
        id=UseCaseIdentifier.UC_ADMIN_UNASSIGNES_TEMPLATE_FROM_PROJECT,
        group=UseCaseGroup.CONFIGURATION,
        apiName="adminUnassignTemplateFromProject",
        title="Admin unassigns template from project",
        description="An administrator unassigns a template from a project")
public @interface UseCaseAdminUnassignsTemplateFromProject {

	Step value();
}
/* @formatter:on */
