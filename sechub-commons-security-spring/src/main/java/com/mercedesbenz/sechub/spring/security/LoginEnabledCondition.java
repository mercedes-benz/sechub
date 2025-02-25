// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class LoginEnabledCondition implements Condition {

    private static final Logger logger = LoggerFactory.getLogger(LoginEnabledCondition.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        /* @formatter:off */
        Boolean loginEnabled = context
                .getEnvironment()
                .getProperty("%s.enabled".formatted(SecHubSecurityProperties.LoginProperties.PREFIX), boolean.class, false);
        /* @formatter:on */
        logger.debug("loginEnabled={}", loginEnabled);
        return loginEnabled;
    }
}