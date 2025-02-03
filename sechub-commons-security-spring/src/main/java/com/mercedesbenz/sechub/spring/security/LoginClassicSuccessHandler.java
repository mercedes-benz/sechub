// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import java.io.IOException;
import java.time.Duration;
import java.util.Base64;

import jakarta.servlet.http.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.mercedesbenz.sechub.spring.security.AbstractSecurityConfiguration.BASE_PATH;

/**
 * {@code LoginClassicSuccessHandler} implements
 * {@link AuthenticationSuccessHandler} to provide custom behavior upon
 * successful authentication. This handler redirects the user to the specified
 * <code>redirectUri</code>. Before redirecting, it creates a cookie containing
 * the user's credentials in the form of <code>username:password</code>. The
 * cookie is encrypted using {@link AES256Encryption} and then encoded using Base64.
 *
 * @see AbstractSecurityConfiguration
 *
 * @author hamidonos
 */
class LoginClassicSuccessHandler implements AuthenticationSuccessHandler {

    private static final Duration ONE_HOUR = Duration.ofHours(1);
    private static final Logger log = LoggerFactory.getLogger(LoginClassicSuccessHandler.class);
    private static final Base64.Encoder encoder = Base64.getEncoder();

    private final String redirectUri;
    private final AES256Encryption aes256Encryption;

    LoginClassicSuccessHandler(String redirectUri, AES256Encryption aes256Encryption) {
        this.redirectUri = redirectUri;
        this.aes256Encryption = aes256Encryption;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        String password = userDetails.getPassword().replace("{noop}", "");
        String classicAuthCredentials = "%s:%s".formatted(username, password);
        byte[] classicAuthCredentialsEncrypted = aes256Encryption.encrypt(classicAuthCredentials);
        String classicAuthCredentialsEncoded = encoder.encodeToString(classicAuthCredentialsEncrypted);
        Cookie cookie = CookieHelper.createCookie(AbstractSecurityConfiguration.CLASSIC_AUTH_COOKIE_NAME, classicAuthCredentialsEncoded, ONE_HOUR, BASE_PATH);
        response.addCookie(cookie);
        log.debug("Redirecting to {}", redirectUri);
        response.sendRedirect(redirectUri);
    }
}