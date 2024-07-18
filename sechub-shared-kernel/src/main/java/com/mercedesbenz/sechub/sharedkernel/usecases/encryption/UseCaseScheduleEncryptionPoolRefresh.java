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
		id=UseCaseIdentifier.UC_SCHEDULE_ENCRYPTION_POOL_REFRESH,
		group=UseCaseGroup.ENCRYPTION,
		apiName="serverStartsEncryptionPoolRefresh",
		title="Scheduler encryption pool refresh",
		description="The scheduler refreshes its encryption pool data to handle new setup")
public @interface UseCaseScheduleEncryptionPoolRefresh{

	Step value();
}
/* @formatter:on */
