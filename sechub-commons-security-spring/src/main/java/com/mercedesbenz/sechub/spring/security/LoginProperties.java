package com.mercedesbenz.sechub.spring.security;

// SPDX-License-Identifier: MIT
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = LoginProperties.PREFIX)
public class LoginProperties {

    static final String PREFIX = "sechub.security.login";
    private static final String CLASSIC = "classic";
    private static final String OAUTH2 = "oauth2";

    private final boolean isEnabled;
    private final String redirectUri;
    private final List<String> modes;

    @ConstructorBinding
    LoginProperties(boolean enabled, String redirectUri, List<String> modes) {
        this.isEnabled = enabled;
        this.redirectUri = redirectUri;
        this.modes = modes;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public List<String> getModes() {
        return modes;
    }

    public boolean isClassicModeEnabled() {
        return modes.contains(CLASSIC);
    }

    public boolean isOAuth2ModeEnabled() {
        return modes.contains(OAUTH2);
    }
}
