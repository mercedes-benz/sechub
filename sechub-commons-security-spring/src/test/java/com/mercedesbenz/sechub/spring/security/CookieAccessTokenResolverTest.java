// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

class CookieAccessTokenResolverTest {

    private static final String MISSING_ACCESS_TOKEN = "missing-access-token";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String ENCRYPTED_ACCESS_TOKEN = "encrypted-access-token";
    private static final String ENCRYPTED_ACCESS_TOKEN_B64_ENCODED = Base64.getEncoder().encodeToString(ENCRYPTED_ACCESS_TOKEN.getBytes());
    private static final byte[] DECRYPTED_ACCESS_TOKEN_B64_DECODED = Base64.getDecoder().decode(ENCRYPTED_ACCESS_TOKEN_B64_ENCODED);
    private static final String BASE_PATH = "/";

    private static final AES256Encryption aes256Encryption = mock();
    private static final CookieAccessTokenResolver cookieAccessTokenResolver = new CookieAccessTokenResolver(aes256Encryption);
    private static final HttpServletRequest httpServletRequest = mock();

    @BeforeEach
    void beforeEach() {
        Mockito.reset(aes256Encryption);
        when(aes256Encryption.decrypt(any())).thenReturn(ACCESS_TOKEN);
    }

    @Test
    void resolve_reads_and_decrypts_access_token_from_cookies_successfully() {
        /* prepare */
        Cookie cookie = createAccessTokenCookie(ACCESS_TOKEN_KEY, ENCRYPTED_ACCESS_TOKEN_B64_ENCODED);
        Cookie someOtherCookie = createAccessTokenCookie("some-other-cookie-name", "some-other-cookie-value");
        Cookie[] cookies = List.of(cookie, someOtherCookie).toArray(new Cookie[0]);
        when(httpServletRequest.getCookies()).thenReturn(cookies);

        /* execute */
        String accessToken = cookieAccessTokenResolver.resolve(httpServletRequest);

        /* test */
        assertThat(accessToken).isEqualTo(ACCESS_TOKEN);
        verify(aes256Encryption).decrypt(DECRYPTED_ACCESS_TOKEN_B64_DECODED);
    }

    @ParameterizedTest
    @ArgumentsSource(CookieAccessTokenResolverTest.InvalidCookieListProvider.class)
    void resolve_returns_missing_access_token_value_when_access_token_cookie_is_not_found(List<Cookie> cookies) {
        /* prepare */
        Cookie[] array = cookies == null ? null : cookies.toArray(new Cookie[0]);
        when(httpServletRequest.getCookies()).thenReturn(array);

        /* execute */
        String accessToken = cookieAccessTokenResolver.resolve(httpServletRequest);

        /* test */
        assertThat(accessToken).isEqualTo(MISSING_ACCESS_TOKEN);
    }

    @Test
    void resolve_returns_missing_access_token_value_when_access_token_decoding_fails() {
        /* prepare */
        Cookie accessTokenCookie = createAccessTokenCookie(ACCESS_TOKEN_KEY, ENCRYPTED_ACCESS_TOKEN_B64_ENCODED.concat("-invalid-b64"));
        Cookie[] cookies = List.of(accessTokenCookie).toArray(new Cookie[0]);
        when(httpServletRequest.getCookies()).thenReturn(cookies);

        /* execute & test */

        String accessToken = cookieAccessTokenResolver.resolve(httpServletRequest);

        /* test */
        assertThat(accessToken).isEqualTo(MISSING_ACCESS_TOKEN);
    }

    @Test
    void resolve_returns_missing_access_token_value_when_access_token_decryption_fails() {
        /* prepare */
        Cookie accessTokenCookie = createAccessTokenCookie(ACCESS_TOKEN_KEY, ENCRYPTED_ACCESS_TOKEN_B64_ENCODED);
        Cookie[] cookies = List.of(accessTokenCookie).toArray(new Cookie[0]);
        when(httpServletRequest.getCookies()).thenReturn(cookies);
        when(aes256Encryption.decrypt(any())).thenThrow(new RuntimeException());

        /* execute & test */

        String accessToken = cookieAccessTokenResolver.resolve(httpServletRequest);

        /* test */
        assertThat(accessToken).isEqualTo(MISSING_ACCESS_TOKEN);
    }

    private static Cookie createAccessTokenCookie(String name, String value) {
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