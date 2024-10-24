// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.Mockito;

import com.mercedesbenz.sechub.webserver.encryption.AES256Encryption;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

class JwtCookieResolverTest {

    private static final String MISSING_JWT_VALUE = "missing-jwt";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String JWT = "jwt";
    private static final String ENCRYPTED_JWT = "encrypted-jwt";
    private static final String ENCRYPTED_JWT_B64_ENCODED = Base64.getEncoder().encodeToString(ENCRYPTED_JWT.getBytes());
    private static final byte[] DECRYPTED_JWT_B64_DECODED = Base64.getDecoder().decode(ENCRYPTED_JWT_B64_ENCODED);
    private static final String BASE_PATH = "/";

    private static final AES256Encryption aes256Encryption = mock();
    private static final JwtCookieResolver jwtCookieResolver = new JwtCookieResolver(aes256Encryption);
    private static final HttpServletRequest httpServletRequest = mock();

    @BeforeEach
    void beforeEach() {
        Mockito.reset(aes256Encryption);
        when(aes256Encryption.decrypt(any())).thenReturn(JWT);
    }

    @Test
    void resolve_reads_and_decrypts_jwt_from_cookies_successfully() {
        // prepare
        Cookie jwtCookie = createJwtCookie(ACCESS_TOKEN, ENCRYPTED_JWT_B64_ENCODED);
        Cookie someOtherCookie = createJwtCookie("some-other-cookie-name", "some-other-cookie-value");
        Cookie[] cookies = List.of(jwtCookie, someOtherCookie).toArray(new Cookie[0]);
        when(httpServletRequest.getCookies()).thenReturn(cookies);

        // execute
        String jwt = jwtCookieResolver.resolve(httpServletRequest);

        // test
        assertThat(jwt).isEqualTo(JWT);
        verify(aes256Encryption).decrypt(DECRYPTED_JWT_B64_DECODED);
    }

    @ParameterizedTest
    @ArgumentsSource(JwtCookieResolverTest.InvalidCookieListProvider.class)
    void resolve_returns_missing_jwt_value_when_jwt_cookie_is_not_found(List<Cookie> cookies) {
        // prepare
        Cookie[] array = cookies == null ? null : cookies.toArray(new Cookie[0]);
        when(httpServletRequest.getCookies()).thenReturn(array);

        // execute
        String jwt = jwtCookieResolver.resolve(httpServletRequest);

        // test
        assertThat(jwt).isEqualTo(MISSING_JWT_VALUE);
    }

    @Test
    void resolve_returns_missing_jwt_value_when_access_token_decoding_fails() {
        // prepare
        Cookie jwtCookie = createJwtCookie(ACCESS_TOKEN, ENCRYPTED_JWT_B64_ENCODED.concat("-invalid-b64"));
        Cookie[] cookies = List.of(jwtCookie).toArray(new Cookie[0]);
        when(httpServletRequest.getCookies()).thenReturn(cookies);

        // execute & test

        String jwt = jwtCookieResolver.resolve(httpServletRequest);

        // test
        assertThat(jwt).isEqualTo(MISSING_JWT_VALUE);
    }

    @Test
    void resolve_returns_missing_jwt_value_when_access_token_decryption_fails() {
        // prepare
        Cookie jwtCookie = createJwtCookie(ACCESS_TOKEN, ENCRYPTED_JWT_B64_ENCODED);
        Cookie[] cookies = List.of(jwtCookie).toArray(new Cookie[0]);
        when(httpServletRequest.getCookies()).thenReturn(cookies);
        when(aes256Encryption.decrypt(any())).thenThrow(new RuntimeException());

        // execute & test

        String jwt = jwtCookieResolver.resolve(httpServletRequest);

        // test
        assertThat(jwt).isEqualTo(MISSING_JWT_VALUE);
    }

    private static Cookie createJwtCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(3600);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath(BASE_PATH);
        return cookie;
    }

    static class InvalidCookieListProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            // @formatter:off
            return Stream.of(
                    Arguments.of((Object) null),
                    Arguments.of(List.of()),
                    Arguments.of(List.of(createJwtCookie("invalid-cookie-name", ENCRYPTED_JWT_B64_ENCODED)))
            );
            // @formatter:on
        }
    }
}