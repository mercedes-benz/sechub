// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.lang.reflect.Method;
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

/**
 * Controller for the login page.
 *
 * <p>
 * The controller is conditionally enabled based on the
 * {@link LoginEnabledCondition}.
 * </p>
 * <p>
 * The controller supports two tabs: {@value #OAUTH2_TAB} and
 * {@value #CLASSIC_TAB}. The default tab is {@value #OAUTH2_TAB}. Depending on
 * the configuration of {@link SecHubSecurityProperties} one of these or both
 * tabs can be enabled.
 * </p>
 * <p>
 * The controller also supports two themes: {@value #DEFAULT_THEME} and
 * {@value #JETBRAINS_THEME}. The default theme is {@value #DEFAULT_THEME}.
 * </p>
 * <p>
 * A redirect URI can be specified as a query parameter. It'll be used to
 * redirect the user after a successful login.
 * </p>
 *
 * @author hamidonos
 */
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

    /* @formatter:off */
    String login(Model model,
                 @RequestParam(name = "tab", required = false, defaultValue = "") String tab,
                 @RequestParam(required = false, defaultValue = DEFAULT_THEME) String theme,
                 @RequestParam(required = false, defaultValue = "") String redirectPath) {

        // TODO: alternatively create a other controller api for every plugin that sets the redirectUri hard coded
        // TODO: alternatively whitelist the redirectPath so that no redirect to other domains is possible

        /* @formatter:on */
        if (!Set.of(JETBRAINS_THEME, DEFAULT_THEME).contains(theme)) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid theme");
        }

        model.addAttribute("isOAuth2Enabled", isOAuth2Enabled);
        model.addAttribute("isClassicAuthEnabled", isClassicAuthEnabled);
        model.addAttribute("theme", theme);
        model.addAttribute("redirectUri", redirectPath);

        if (isOAuth2Enabled) {
            String registrationId = loginProperties.getOAuth2Properties().getProvider();
            model.addAttribute("registrationId", registrationId);
        }

        if (!ALLOWED_TABS.contains(tab) && isOAuth2Enabled) {
            return ("redirect:%s?tab=%s&theme=%s&redirectUri=%s").formatted(loginProperties.getLoginPage(), OAUTH2_TAB, theme, redirectPath);
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

        Method loginMethod = getClass().getDeclaredMethod("login", Model.class, String.class, String.class, String.class);
        requestMappingHandlerMapping.registerMapping(requestMappingInfo, this, loginMethod);
    }
}