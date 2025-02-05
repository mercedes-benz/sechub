// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import java.util.Set;

import org.springframework.context.annotation.Conditional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Controller
@Conditional(LoginEnabledCondition.class)
class LoginController {

    private static final String OAUTH2_TAB = "oauth2";
    private static final String CLASSIC_TAB = "classic";
    private static final Set<String> ALLOWED_TABS = Set.of(OAUTH2_TAB, CLASSIC_TAB);
    private static final String DEFAULT_THEME = "default";
    private static final String JETBRAINS_THEME = "jetbrains";

    private final SecHubSecurityProperties.LoginProperties loginProperties;
    private final boolean isOAuth2Enabled;
    private final boolean isClassicAuthEnabled;

    /* @formatter:off */
    LoginController(RequestMappingHandlerMapping requestMappingHandlerMapping,
                    SecHubSecurityProperties secHubSecurityProperties) throws NoSuchMethodException {
        loginProperties = secHubSecurityProperties.getLoginProperties();
        /* register the login page dynamically at runtime */
        registerLoginMapping(requestMappingHandlerMapping, loginProperties.getLoginPage());
        this.isOAuth2Enabled = loginProperties.isOAuth2ModeEnabled();
        this.isClassicAuthEnabled = loginProperties.isClassicModeEnabled();
    }
    /* @formatter:on */

    String login(Model model, @RequestParam(name = "tab", required = false, defaultValue = "") String tab, @RequestParam(required = false, defaultValue = DEFAULT_THEME) String theme) {
        if (!Set.of(JETBRAINS_THEME, DEFAULT_THEME).contains(theme)) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid theme");
        }

        model.addAttribute("theme", theme);
        model.addAttribute("isOAuth2Enabled", isOAuth2Enabled);
        model.addAttribute("isClassicAuthEnabled", isClassicAuthEnabled);

        if (loginProperties != null && isOAuth2Enabled) {
            String registrationId = loginProperties.getOAuth2Properties().getProvider();
            model.addAttribute("registrationId", registrationId);
        }

        if (!ALLOWED_TABS.contains(tab) && isOAuth2Enabled) {
            return "redirect:/login?tab=%s".formatted(OAUTH2_TAB);
        }

        return "login";
    }

    private void registerLoginMapping(RequestMappingHandlerMapping requestMappingHandlerMapping, String loginPage) throws NoSuchMethodException {
        /* @formatter:off */
        RequestMappingInfo requestMappingInfo = RequestMappingInfo
                .paths(loginPage)
                .methods(RequestMethod.GET)
                .produces(MediaType.TEXT_HTML_VALUE)
                .build();
        /* @formatter:on */

        requestMappingHandlerMapping.registerMapping(requestMappingInfo, this, getClass().getDeclaredMethod("login", Model.class, String.class));
    }
}