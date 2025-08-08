package com.mercedesbenz.sechub.sharedkernel.assistant;

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
id = UseCaseIdentifier.UC_USER_REQUESTS_FINDING_EXPLANATION,
group = UseCaseGroup.USER_SELF_SERVICE,
apiName = "userRequestFindingExplanation",
title = "User requests finding explanation",
description = "In this usecase the user requests an explanation for a finding.")
public @interface UseCaseUserRequestFindingExplanation {

	Step value();
}
/* @formatter:on */