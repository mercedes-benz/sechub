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
		id=UseCaseIdentifier.UC_ADMIN_STARTS_ENCRYPTION_ROTATION,
		group=UseCaseGroup.ENCRYPTION,
		apiName="adminStartsEncryptionRotation",
		title="Admin starts encryption rotation",
		description="An administrator starts encryption rotation.")
public @interface UseCaseAdminStartsEncryptionRotation{

	Step value();
}
/* @formatter:on */
