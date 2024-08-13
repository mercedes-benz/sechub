// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.encryption;

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
		id=UseCaseIdentifier.UC_ADMIN_FETCHES_ENCRYPTION_STATUS,
		group=UseCaseGroup.ENCRYPTION,
		apiName="adminFetchesEncryptionStatus",
		title="Admin fetches encryption status",
		description="An administrator fetches encryption status from all domains where encryption is used.")
public @interface UseCaseAdminFetchesEncryptionStatus{

	Step value();
}
/* @formatter:on */
