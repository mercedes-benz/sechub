// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.usecases.admin.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.UseCaseDefinition;
import com.daimler.sechub.sharedkernel.usecases.UseCaseGroup;
import com.daimler.sechub.sharedkernel.usecases.UseCaseIdentifier;

/* @formatter:off */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@UseCaseDefinition(
		id=UseCaseIdentifier.UC_ADMIN_UPDATES_MAPPING_CONFIGURATION,
		group=UseCaseGroup.CONFIGURATION,
		apiName="administratorUpdatesMappingConfiguration",
		title="Admin updates mapping configuration",
		description="An administrator changes mapping configuration. Mappings represents a generic mechanism to replace a given string, matched by configured regular expression pattern with a replacement string. Some of the mappings are used for adapter behaviour.")
public @interface UseCaseAdministratorUpdatesMappingConfiguration{

	Step value();
}
/* @formatter:on */
