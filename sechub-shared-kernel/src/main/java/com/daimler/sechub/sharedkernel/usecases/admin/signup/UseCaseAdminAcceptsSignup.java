// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.usecases.admin.signup;

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
id = UseCaseIdentifier.UC_ADMIN_ACCEPTS_SIGNUP,
group = UseCaseGroup.SIGN_UP,
apiName = "adminAcceptsSignup",
title = "Admin applies self registration",
description = "In this usecase the administrator will accept the self registration done by an user.")
public @interface UseCaseAdminAcceptsSignup {

	Step value();
}
/* @formatter:on */
