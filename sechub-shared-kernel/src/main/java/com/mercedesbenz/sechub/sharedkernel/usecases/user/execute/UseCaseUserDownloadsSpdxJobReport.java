// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.user.execute;

import static com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseIdentifier.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseDefinition;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseGroup;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@UseCaseDefinition(id = UC_USER_GET_SPDX_JOB_REPORT, group = UseCaseGroup.SECHUB_EXECUTION, apiName = "userDownloadsSpdxJobReport", title = "User downloads job report in SPDX format", description = "user/download_spdx_report_description.adoc")
public @interface UseCaseUserDownloadsSpdxJobReport {

    Step value();
}
/* @formatter:on */
