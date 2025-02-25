// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;

public class LoginModeOAuth2ActiveCondition implements Condition {

    private static final Logger logger = LoggerFactory.getLogger(LoginOAuth2AccessTokenClient.class);

    private static final String SEARCH_PROPERTY = SecHubSecurityProperties.LoginProperties.PREFIX + "." + SecHubSecurityProperties.LoginProperties.MODES;

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

        Environment environment = context.getEnvironment();

        String value = environment.getProperty(SEARCH_PROPERTY);
        logger.debug("Directly fetched: {}='{}'", SEARCH_PROPERTY, value);

        List<String> list = SimpleStringUtils.createListForCommaSeparatedValues(value);

        boolean isOAuth2ModeEnabled = list.contains(SecHubSecurityProperties.LoginProperties.OAUTH2_MODE);

        logger.debug("isAuth2ModeEnabled={}", isOAuth2ModeEnabled);
        return isOAuth2ModeEnabled;
    }
}