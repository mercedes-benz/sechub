// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequestWrapper;

class ClassicAuthCredentialsCookieFilterTest {

    private static final AES256Encryption aes256encryption = mock();
    private static final ClassicAuthCredentialsCookieFilter filterToTest = new ClassicAuthCredentialsCookieFilter(aes256encryption);
    private static final Base64.Encoder b64Encoder = Base64.getEncoder();
    private static final Base64.Decoder b64Decoder = Base64.getDecoder();
    private static final String ENCRYPTED_VALUE = b64Encoder.encodeToString("someEncryptedValue".getBytes(StandardCharsets.UTF_8));
    private static final String DECRYPTED_VALUE = "someDecryptedValue";
    private static final Cookie oAuth2Cookie = CookieHelper.createCookie(AbstractSecurityConfiguration.OAUTH2_COOKIE_NAME, ENCRYPTED_VALUE, Duration.ofHours(1),
            AbstractSecurityConfiguration.BASE_PATH);
    private static final Cookie classicAuthCookie = CookieHelper.createCookie(AbstractSecurityConfiguration.CLASSIC_AUTH_COOKIE_NAME, ENCRYPTED_VALUE,
            Duration.ofHours(1), AbstractSecurityConfiguration.BASE_PATH);

    @BeforeEach
    void beforeEach() {
        reset(aes256encryption);
        when(aes256encryption.decrypt(any())).thenReturn(DECRYPTED_VALUE);
        when(aes256encryption.encrypt(any())).thenReturn(ENCRYPTED_VALUE.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void do_filter_internal_with_no_classic_auth_cookie_found_skips_execution() throws ServletException, IOException {
        try (MockedStatic<CookieHelper> cookieHelper = mockStatic(CookieHelper.class)) {

            /* prepare */
            MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
            httpServletRequest.setCookies(oAuth2Cookie);
            MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
            FilterChain filterChain = mock();

            /* execute */
            filterToTest.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

            /* test */
            verify(filterChain).doFilter(assertArg(request -> assertThat(request).isNotInstanceOf(HttpServletRequestWrapper.class)), eq(httpServletResponse));
            cookieHelper.verify(() -> CookieHelper.removeCookie(any(), anyString()), VerificationModeFactory.times(0));
            verifyNoInteractions(aes256encryption);
        }
    }

    @Test
    void do_filter_internal_with_classic_auth_cookie_and_oauth2_cookie_present_removes_classic_auth_cookie() throws ServletException, IOException {
        try (MockedStatic<CookieHelper> cookieHelper = mockStatic(CookieHelper.class)) {

            /* prepare */
            MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
            httpServletRequest.setCookies(oAuth2Cookie, classicAuthCookie);
            MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
            FilterChain filterChain = mock();
            spyCookieHelper(cookieHelper, httpServletRequest);

            /* execute */
            filterToTest.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

            /* test */
            verify(filterChain).doFilter(assertArg(request -> assertThat(request).isNotInstanceOf(HttpServletRequestWrapper.class)), eq(httpServletResponse));
            cookieHelper.verify(() -> CookieHelper.removeCookie(httpServletResponse, classicAuthCookie.getName()));
            verifyNoInteractions(aes256encryption);
        }
    }

    @Test
    void do_filter_internal_with_classic_auth_cookie_present_creates_basic_auth_http_request_wrapper() throws ServletException, IOException {
        try (MockedStatic<CookieHelper> cookieHelper = mockStatic(CookieHelper.class)) {

            /* prepare */
            MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
            httpServletRequest.setCookies(classicAuthCookie);
            MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
            FilterChain filterChain = mock();
            spyCookieHelper(cookieHelper, httpServletRequest);

            /* execute */
            filterToTest.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

            /* test */
            String classicAuthCookieValue = classicAuthCookie.getValue();
            verify(aes256encryption).decrypt(b64Decoder.decode(classicAuthCookieValue));
            verify(filterChain).doFilter(assertArg(requestWrapper -> {
                HttpServletRequestWrapper httpServletRequestWrapper = (HttpServletRequestWrapper) requestWrapper;
                assertThat(httpServletRequestWrapper).isNotNull();
                assertThat(httpServletRequestWrapper.getCookies()).isEqualTo(new Cookie[] { classicAuthCookie });
                String basicAuthHeader = httpServletRequestWrapper.getHeader(HttpHeaders.AUTHORIZATION);
                String expectedBasicAuthHeader = "Basic " + b64Encoder.encodeToString(DECRYPTED_VALUE.getBytes(StandardCharsets.UTF_8));
                assertThat(basicAuthHeader).isEqualTo(expectedBasicAuthHeader);
            }), eq(httpServletResponse));
        }
    }

    private static void spyCookieHelper(MockedStatic<CookieHelper> cookieHelper, MockHttpServletRequest httpServletRequest) {
        cookieHelper.when(() -> CookieHelper.getCookie(httpServletRequest, AbstractSecurityConfiguration.OAUTH2_COOKIE_NAME)).thenCallRealMethod();
        cookieHelper.when(() -> CookieHelper.getCookie(httpServletRequest, AbstractSecurityConfiguration.CLASSIC_AUTH_COOKIE_NAME)).thenCallRealMethod();
    }
}