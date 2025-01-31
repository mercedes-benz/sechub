// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

class ClassicAuthCredentialsCookieFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ClassicAuthCredentialsCookieFilter.class);
    private static final Base64.Decoder b64Decoder = Base64.getDecoder();
    private static final Base64.Encoder b64Encoder = Base64.getEncoder();

    private final AES256Encryption aes256Encryption;

    public ClassicAuthCredentialsCookieFilter(AES256Encryption aes256Encryption) {
        this.aes256Encryption = aes256Encryption;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Optional<Cookie> optOAuth2Cookie = CookieHelper.getCookie(request, AbstractSecurityConfiguration.OAUTH2_COOKIE_NAME);
        Optional<Cookie> optClassicAuthCookie = CookieHelper.getCookie(request, AbstractSecurityConfiguration.CLASSIC_AUTH_COOKIE_NAME);

        /*
         * Skip this filter if no Classic Auth cookie is present
         */
        if (optClassicAuthCookie.isEmpty()) {
            logger.debug("No Classic Auth cookie found. Skipping this filter.");
            filterChain.doFilter(request, response);
            return;
        }

        /*
         * If both OAuth2 and Classic Auth cookies are present, remove the Classic Auth cookie
         * OAuth2 has higher priority
         */
        if (optOAuth2Cookie.isPresent()) {
            logger.warn("Found both OAuth2 and Classic Auth cookies! Classic Auth cookie will be removed.");
            CookieHelper.removeCookie(response, AbstractSecurityConfiguration.CLASSIC_AUTH_COOKIE_NAME);
            filterChain.doFilter(request, response);
            return;
        }

        String credentials = decryptCredentials(optClassicAuthCookie.get().getValue());
        HttpServletRequestWrapper basicAuthHttpRequestWrapper = createBasicAuthHttpRequestWrapper(request, credentials);
        filterChain.doFilter(basicAuthHttpRequestWrapper, response);
    }

    private String decryptCredentials(String encryptedCredentials) {
        byte[] encryptedCredentialsB64Decoded = b64Decoder.decode(encryptedCredentials);
        return aes256Encryption.decrypt(encryptedCredentialsB64Decoded);
    }

    private HttpServletRequestWrapper createBasicAuthHttpRequestWrapper(HttpServletRequest request, String credentials) {
        String basicAuthHeader = createBasicAuthHeader(credentials);
        return new BasicAuthHttpRequestWrapper(request, basicAuthHeader);
    }

    private static String createBasicAuthHeader(String credentials) {
        String credentialsB64Encoded = b64Encoder.encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + credentialsB64Encoded;
    }

    private static class BasicAuthHttpRequestWrapper extends HttpServletRequestWrapper {

        private final String basicAuthHeader;

        public BasicAuthHttpRequestWrapper(HttpServletRequest request, String basicAuthHeader) {
            super(request);
            this.basicAuthHeader = basicAuthHeader;
        }

        @Override
        public String getHeader(String name) {
            if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name)) {
                return basicAuthHeader;
            }

            return super.getHeader(name);
        }
    }

}
