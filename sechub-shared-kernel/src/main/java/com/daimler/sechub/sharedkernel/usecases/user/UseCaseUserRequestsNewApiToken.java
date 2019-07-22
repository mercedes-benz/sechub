// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.usecases.user;

import static com.daimler.sechub.sharedkernel.usecases.UseCaseIdentifier.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.UseCaseDefinition;
import com.daimler.sechub.sharedkernel.usecases.UseCaseGroup;
/* @formatter:off */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@UseCaseDefinition(
		id=UC_USER_REQUESTS_NEW_APITOKEN,
		group=UseCaseGroup.SIGN_UP,
		title="User requests new API token",
		description="user/request_new_api_token_description.adoc")
public @interface UseCaseUserRequestsNewApiToken {

	Step value();
}
/* @formatter:on */
