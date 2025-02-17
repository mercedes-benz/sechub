// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class LoginModeOAuth2ActiveCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, @SuppressWarnings("NullableProblems") AnnotatedTypeMetadata metadata) {
        boolean isOAuth2ModeEnabled = false;
        String mode;
        int index = 0;

        do {
            /* @formatter:off */
            mode = context
                    .getEnvironment()
                    .getProperty("%s.%s[%d]".formatted(SecHubSecurityProperties.LoginProperties.PREFIX,SecHubSecurityProperties.LoginProperties.MODES, index++));
            /* @formatter:on */

            if (SecHubSecurityProperties.LoginProperties.OAUTH2_MODE.equals(mode)) {
                isOAuth2ModeEnabled = true;
            }
        } while (mode != null);

        return isOAuth2ModeEnabled;
    }
}