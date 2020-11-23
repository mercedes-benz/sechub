// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.usecases.admin.status;

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
		id=UseCaseIdentifier.UC_ADMIN_RECEIVES_NOTIFICATOIN_ABOUT_CLUSTER_MEMBER_START,
		group=UseCaseGroup.TECHNICAL,
		apiName="administratorReceivesNotificationAboutNewchedulerInstanceStart", //TODO: Rename
		title="Admin receives notification about start of a new scheduler instance",
		description="admin/notification_about_scheduler_start.adoc")
public @interface UseCaseAdministratorReceivesNotificationAboutNewchedulerInstanceStart {

	Step value();
}
/* @formatter:on */