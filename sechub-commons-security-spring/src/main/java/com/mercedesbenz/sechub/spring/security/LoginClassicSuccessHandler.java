// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static com.mercedesbenz.sechub.spring.security.AbstractSecurityConfiguration.BASE_PATH;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.time.Duration;
import java.util.Base64;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * {@code LoginClassicSuccessHandler} implements
 * {@link AuthenticationSuccessHandler} to provide custom behavior upon
 * successful authentication. This handler redirects the user to the specified
 * <code>redirectUri</code>. Before redirecting, it creates a cookie containing
 * the user's credentials in the form of <code>username:password</code>. The
 * cookie is encrypted using {@link AES256Encryption} and then encoded using
 * Base64.
 *
 * @see AbstractSecurityConfiguration
 *
 * @author hamidonos
 */
class LoginClassicSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(LoginClassicSuccessHandler.class);
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String BASIC_AUTH_CREDENTIALS_FORMAT = "%s:%s";
    private static final Base64.Encoder encoder = Base64.getEncoder();

    private final String defaultRedirectUri;
    private final AES256Encryption aes256Encryption;
    private final Duration cookieAge;

    LoginClassicSuccessHandler(AES256Encryption aes256Encryption, Duration cookieAge, String defaultRedirectUri) {
        this.aes256Encryption = aes256Encryption;
        this.cookieAge = cookieAge;
        this.defaultRedirectUri = requireNonNull(defaultRedirectUri, "Property defaultRedirectUri must not be null");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String username = request.getParameter(USERNAME);
        String password = request.getParameter(PASSWORD);
        String credentials = BASIC_AUTH_CREDENTIALS_FORMAT.formatted(username, password);
        byte[] credentialsEncrypted = aes256Encryption.encrypt(credentials);
        String credentialsEncoded = encoder.encodeToString(credentialsEncrypted);
        Cookie cookie = CookieHelper.createCookie(AbstractSecurityConfiguration.CLASSIC_AUTH_COOKIE_NAME, credentialsEncoded, cookieAge, BASE_PATH);
        response.addCookie(cookie);
        String theme = request.getParameter("theme");
        String redirectUri = request.getParameter("redirectUri");
        if (redirectUri != null) {
            sendRedirect(response, redirectUri, theme);
        } else {
            sendRedirect(response, defaultRedirectUri, theme);
        }
    }

    // TODO: extract common redirect component for this and login oauth2 success handler
    private static void sendRedirect(HttpServletResponse response, String redirectUri, String theme) {
        try {
            log.debug("Redirecting to {}", redirectUri);
            response.sendRedirect(redirectUri + "?theme=" + theme);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}