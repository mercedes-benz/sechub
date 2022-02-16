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
		id=PDSUseCaseIdentifier.UC_ADMIN_FETCHES_MONITORING_STATUS,
		group=PDSUseCaseGroup.MONITORING,
		title="Admin fetches monitoring status",
		description="An administrator fetches current state of cluster members,jobs running and also of execution service of dedicated memers. So is able to check queue fill state, jobs running etc.")
public @interface UseCaseAdminFetchesMonitoringStatus {
    PDSStep value();
}
/* @formatter:on */
