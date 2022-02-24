// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.autocleanup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mercedesbenz.sechub.sharedkernel.APIConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseDefinition;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseGroup;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseIdentifier;

/* @formatter:off */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@UseCaseDefinition(
		id=UseCaseIdentifier.UC_ADMINISTRATION_AUTO_CLEANUP_EXECUTION,
		group=UseCaseGroup.TECHNICAL,
		apiName=APIConstants.NO_API_AVAILABLE,
		title="Sechub administration domain auto cleanup",
		description="The administration domain does execute an auto cleanup. Periodically it will be checked if there is any outdated job information. The timeperiod is detrmine")
public @interface UseCaseAdministrationAutoCleanExecution {

	Step value();
}
/* @formatter:on */
