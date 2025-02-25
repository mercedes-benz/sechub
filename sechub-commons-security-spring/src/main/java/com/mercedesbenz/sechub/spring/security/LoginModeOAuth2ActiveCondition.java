// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class LoginModeOAuth2ActiveCondition implements Condition {

    private static final Logger logger = LoggerFactory.getLogger(LoginOAuth2AccessTokenClient.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        boolean isOAuth2ModeEnabled = false;

        String toSearch = "%s.%s".formatted(SecHubSecurityProperties.LoginProperties.PREFIX, SecHubSecurityProperties.LoginProperties.MODES);

        Environment environment = context.getEnvironment();
        logger.debug("Directly fetched: {}='{}'", toSearch, environment.getProperty(toSearch));

        String mode;
        int index = 0;

        do {
            String propertyName = "%s.%s[%d]".formatted(SecHubSecurityProperties.LoginProperties.PREFIX, SecHubSecurityProperties.LoginProperties.MODES,
                    index++);
            mode = environment.getProperty(propertyName);
            logger.debug("For property '{}' context retuned: '{}'", propertyName, mode);

            if (SecHubSecurityProperties.LoginProperties.OAUTH2_MODE.equals(mode)) {
                isOAuth2ModeEnabled = true;
            }
        } while (mode != null);

        logger.debug("isAuth2ModeEnabled={}", isOAuth2ModeEnabled);

        return isOAuth2ModeEnabled;
    }
}