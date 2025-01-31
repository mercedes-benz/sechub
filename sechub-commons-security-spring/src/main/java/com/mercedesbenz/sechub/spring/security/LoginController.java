// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import org.springframework.context.annotation.Conditional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Set;

@Controller
@Conditional(LoginEnabledCondition.class)
class LoginController {

    private static final String OAUTH2_TAB = "oauth2";
    private static final String CLASSIC_TAB = "classic";
    private static final Set<String> TABS = Set.of(OAUTH2_TAB, CLASSIC_TAB);
    private static final String REDIRECT_LOGIN_WITH_TAB_FORMAT = "redirect:/login?tab=%s";
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

    String login(Model model, @RequestParam(required = false, defaultValue = "") String tab){
        model.addAttribute("isOAuth2Enabled", isOAuth2Enabled);
        model.addAttribute("isClassicAuthEnabled", isClassicAuthEnabled);

        if (loginProperties != null && isOAuth2Enabled) {
            String registrationId = loginProperties.getOAuth2Properties().getProvider();
            model.addAttribute("registrationId", registrationId);
        }

        if (!TABS.contains(tab) && isOAuth2Enabled) {
            return REDIRECT_LOGIN_WITH_TAB_FORMAT.formatted(OAUTH2_TAB);
        }

        return "login";
    }

    private void registerLoginMapping(RequestMappingHandlerMapping requestMappingHandlerMapping, String loginPage) throws NoSuchMethodException {
        RequestMappingInfo requestMappingInfo = RequestMappingInfo.paths(loginPage).methods(RequestMethod.GET).produces(MediaType.APPLICATION_JSON_VALUE)
                .build();

        requestMappingHandlerMapping.registerMapping(requestMappingInfo, this, getClass().getDeclaredMethod("login", Model.class, String.class));
    }
}