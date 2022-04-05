// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.usecase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/* @formatter:off */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PDSUseCaseDefinition(
		id=PDSUseCaseIdentifier.UC_ADMIN_FETCHES_JOB_RESULT_OR_FAILURE_TEXT,
		group=PDSUseCaseGroup.JOB_EXECUTION,
		title="Admin fetches job result",
		description="Same as when a user is fetching a job result. "+
		"But will return results in any kind of state.\n"+
		"If failure happens an admin is able to load the error output which is contained\n"+
		"instead of result - but it's no JSON!\n\nSo only administrator is able to get insights"+
		"to processes and system logs, but no normal user.")
public @interface UseCaseAdminFetchesJobResultOrFailureText {
    PDSStep value();
}
/* @formatter:on */
