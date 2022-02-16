// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.usecases.user.execute;

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
		id=UseCaseIdentifier.UC_USER_USES_CLIENT_TO_SCAN,
		group=UseCaseGroup.SECHUB_EXECUTION,
		apiName="userStartsSynchronousScanByClient",
		title="User starts scan by client",
		description="user/start_scan_by_client_description.adoc")
public @interface UseCaseUserStartsSynchronousScanByClient {

	Step value();
}
/* @formatter:on */
