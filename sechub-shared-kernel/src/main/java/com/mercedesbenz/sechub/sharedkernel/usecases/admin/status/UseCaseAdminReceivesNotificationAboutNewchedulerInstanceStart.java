// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.admin.status;

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
		id=UseCaseIdentifier.UC_ADMIN_RECEIVES_NOTIFICATION_ABOUT_CLUSTER_MEMBER_START,
		group=UseCaseGroup.TECHNICAL,
		apiName="adminReceivesNotificationAboutNewchedulerInstanceStart",
		title="Admin receives notification about start of a new scheduler instance",
		description="admin/notification_about_scheduler_start.adoc")
public @interface UseCaseAdminReceivesNotificationAboutNewchedulerInstanceStart {

	Step value();
}
/* @formatter:on */