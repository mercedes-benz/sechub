// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.user;

import static com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseIdentifier.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseDefinition;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseGroup;

/* @formatter:off */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@UseCaseDefinition(
		id=UC_USER_CLICKS_LINK_TO_GET_NEW_API_TOKEN,
		group=UseCaseGroup.SIGN_UP,
		apiName="userClicksLinkToGetNewAPIToken",
		title="User clicks link to get new api token",
		description="user/clicks_link_to_get_new_api_token.adoc")
public @interface UseCaseUserClicksLinkToGetNewAPIToken {

	Step value();
}
/* @formatter:on */
