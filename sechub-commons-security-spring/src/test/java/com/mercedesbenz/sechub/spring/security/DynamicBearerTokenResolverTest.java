// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

class DynamicBearerTokenResolverTest {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "SECHUB_OAUTH2_ACCESS_TOKEN";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String ENCRYPTED_ACCESS_TOKEN = "encrypted-access-token";
    private static final String ENCRYPTED_ACCESS_TOKEN_B64_ENCODED = Base64.getEncoder().encodeToString(ENCRYPTED_ACCESS_TOKEN.getBytes());
    private static final byte[] DECRYPTED_ACCESS_TOKEN_B64_DECODED = Base64.getDecoder().decode(ENCRYPTED_ACCESS_TOKEN_B64_ENCODED);
    private static final Duration DEFAULT_EXPIRY = Duration.ofMinutes(1);
    private static final String BASE_PATH = "/";

    private static final AES256Encryption aes256Encryption = mock();
    private static final DynamicBearerTokenResolver bearerTokenResolverToTest = new DynamicBearerTokenResolver(aes256Encryption);
    private static final HttpServletRequest httpServletRequest = mock();

    @BeforeEach
    void beforeEach() {
        reset(aes256Encryption, httpServletRequest);
        when(aes256Encryption.decrypt(any())).thenReturn(ACCESS_TOKEN);
    }

    @Test
    void resolve_reads_access_token_from_authorization_header_successfully() {
        /* prepare */
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer " + ACCESS_TOKEN);

        /* execute */
        String accessToken = bearerTokenResolverToTest.resolve(httpServletRequest);

        /* test */
        assertThat(accessToken).isEqualTo(ACCESS_TOKEN);
        verify(httpServletRequest, never()).getCookies();
        verify(aes256Encryption, never()).decrypt(DECRYPTED_ACCESS_TOKEN_B64_DECODED);
    }

    @Test
    void resolve_reads_and_decrypts_access_token_from_cookies_successfully_when_no_authorization_header_is_available() {
        /* prepare */
        when(httpServletRequest.getHeader("Authorization")).thenReturn("");
        Cookie cookie = createAccessTokenCookie(ACCESS_TOKEN_COOKIE_NAME, ENCRYPTED_ACCESS_TOKEN_B64_ENCODED);
        Cookie someOtherCookie = createAccessTokenCookie("some-other-cookie-name", "some-other-cookie-value");
        Cookie[] cookies = List.of(cookie, someOtherCookie).toArray(new Cookie[0]);
        when(httpServletRequest.getCookies()).thenReturn(cookies);

        /* execute */
        String accessToken = bearerTokenResolverToTest.resolve(httpServletRequest);

        /* test */
        assertThat(accessToken).isEqualTo(ACCESS_TOKEN);
        verify(aes256Encryption).decrypt(DECRYPTED_ACCESS_TOKEN_B64_DECODED);
    }

    @ParameterizedTest
    @ArgumentsSource(DynamicBearerTokenResolverTest.InvalidCookieListProvider.class)
    void resolve_returns_null_when_access_token_cookie_is_not_found(List<Cookie> cookies) {
        /* prepare */
        Cookie[] array = cookies == null ? null : cookies.toArray(new Cookie[0]);
        when(httpServletRequest.getCookies()).thenReturn(array);

        /* execute */
        String accessToken = bearerTokenResolverToTest.resolve(httpServletRequest);

        /* test */
        assertThat(accessToken).isEqualTo(null);
    }

    @Test
    void resolve_returns_null_when_access_token_decoding_fails() {
        /* prepare */
        Cookie accessTokenCookie = createAccessTokenCookie(ACCESS_TOKEN_COOKIE_NAME, ENCRYPTED_ACCESS_TOKEN_B64_ENCODED.concat("-invalid-b64"));
        Cookie[] cookies = List.of(accessTokenCookie).toArray(new Cookie[0]);
        when(httpServletRequest.getCookies()).thenReturn(cookies);

        /* execute & test */

        String accessToken = bearerTokenResolverToTest.resolve(httpServletRequest);

        /* test */
        assertThat(accessToken).isEqualTo(null);
    }

    @Test
    void resolve_returns_null_when_access_token_decryption_fails() {
        /* prepare */
        Cookie accessTokenCookie = createAccessTokenCookie(ACCESS_TOKEN_COOKIE_NAME, ENCRYPTED_ACCESS_TOKEN_B64_ENCODED);
        Cookie[] cookies = List.of(accessTokenCookie).toArray(new Cookie[0]);
        when(httpServletRequest.getCookies()).thenReturn(cookies);
        when(aes256Encryption.decrypt(any())).thenThrow(new RuntimeException());

        /* execute & test */

        String accessToken = bearerTokenResolverToTest.resolve(httpServletRequest);

        /* test */
        assertThat(accessToken).isEqualTo(null);
    }

    private static Cookie createAccessTokenCookie(String name, String value) {
        return CookieHelper.createCookie(name, value, DEFAULT_EXPIRY, BASE_PATH);
    }

    static class InvalidCookieListProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            /* @formatter:off */
            return Stream.of(
                    Arguments.of((Object) null),
                    Arguments.of(List.of()),
                    Arguments.of(List.of(createAccessTokenCookie("invalid-cookie-name", ENCRYPTED_ACCESS_TOKEN_B64_ENCODED)))
            );
            /* @formatter:on */
        }
    }
}