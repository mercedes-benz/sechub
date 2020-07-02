// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.usecase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/* @formatter:off */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PDSUseCaseDefinition(
		id=PDSUseCaseIdentifier.UC_ADMIN_FETCHES_EXECUTION_STATUS,
		group=PDSUseCaseGroup.MONITORING,
		title="Admin fetches execution status", 
		description="An administrator fetches current state of execution service. Check queue fill state, jobs running etc.")
public @interface UseCaseAdminFetchesExecutionStatus {
	
}
/* @formatter:on */
