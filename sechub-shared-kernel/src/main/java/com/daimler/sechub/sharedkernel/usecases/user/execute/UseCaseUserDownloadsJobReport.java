// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.usecases.user.execute;

import static com.daimler.sechub.sharedkernel.usecases.UseCaseIdentifier.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.UseCaseDefinition;
import com.daimler.sechub.sharedkernel.usecases.UseCaseGroup;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@UseCaseDefinition(id = UC_USER_GET_JOB_REPORT, group = UseCaseGroup.SECHUB_EXECUTION, apiName = "userDownloadsJobReport", title = "User downloads sechub job report", description = "user/download_sechub_job_report_description.adoc")
public @interface UseCaseUserDownloadsJobReport {

    Step value();
}
/* @formatter:on */
