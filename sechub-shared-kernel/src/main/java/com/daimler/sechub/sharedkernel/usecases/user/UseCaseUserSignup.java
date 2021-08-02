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
		id=UC_SIGNUP,
		group=UseCaseGroup.SIGN_UP,
		apiName="userSignup",
		title="User self registration", 
		description="user/selfregistration_description.adoc")
public @interface UseCaseUserSignup {
	
	Step value();
}
/* @formatter:on */
