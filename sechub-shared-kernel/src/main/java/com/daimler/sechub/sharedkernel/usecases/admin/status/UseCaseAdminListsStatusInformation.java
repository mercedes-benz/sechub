// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.usecases.admin.status;

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
		id=UseCaseIdentifier.UC_ADMIN_LIST_STATUS_INFORMATION,
		group=UseCaseGroup.OTHER,
		apiName="adminListsStatusInformation",
		title="Admin lists status information",
		description="An administrator fetches current known status information about sechub")
public @interface UseCaseAdminListsStatusInformation {

	Step value();
}
/* @formatter:on */
