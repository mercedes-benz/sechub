package com.mercedesbenz.sechub.spring.security;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class LoginEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, @SuppressWarnings("NullableProblems") AnnotatedTypeMetadata metadata) {
        /* @formatter:off */
        return context
                .getEnvironment()
                .getProperty("%s.enabled".formatted(SecurityProperties.Login.PREFIX), boolean.class, false);
        /* @formatter:on */
    }
}