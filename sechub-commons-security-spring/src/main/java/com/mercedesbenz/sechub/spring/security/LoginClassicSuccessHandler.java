// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static com.mercedesbenz.sechub.spring.security.AbstractSecurityConfiguration.BASE_PATH;

import java.io.IOException;
import java.time.Duration;
import java.util.Base64;

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

    private final String redirectUri;
    private final AES256Encryption aes256Encryption;
    private final Duration cookieAge;

    LoginClassicSuccessHandler(AES256Encryption aes256Encryption, Duration cookieAge, String redirectUri) {
        this.redirectUri = redirectUri;
        this.aes256Encryption = aes256Encryption;
        this.cookieAge = cookieAge;
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

        log.debug("Redirecting to {}", redirectUri);
        response.sendRedirect(redirectUri);
    }
}