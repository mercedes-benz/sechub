package com.mercedesbenz.sechub.spring.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CookieHelperTest {

    private static final String COOKIE_NAME = "cookie";
    private static final String COOKIE_VALUE = "value";
    private static final Duration DURATION = Duration.ofHours(1);
    private static final String BASE_PATH = "/";

    @Test
    void create_cookie() {
        /* execute */
        Cookie result = CookieHelper.createCookie(COOKIE_NAME, COOKIE_VALUE, DURATION, BASE_PATH);

        /* test */
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(COOKIE_NAME);
        assertThat(result.getValue()).isEqualTo(COOKIE_VALUE);
        assertThat(result.getMaxAge()).isEqualTo((int) DURATION.getSeconds());
        assertThat(result.isHttpOnly()).isTrue();
        assertThat(result.getSecure()).isTrue();
        assertThat(result.getPath()).isEqualTo(BASE_PATH);
    }

    @Test
    void get_cookie() {
        /* prepare */
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        Cookie cookie = CookieHelper.createCookie(COOKIE_NAME, COOKIE_VALUE, DURATION, BASE_PATH);
        httpServletRequest.setCookies(cookie);

        /* execute */
        Optional<Cookie> result = CookieHelper.getCookie(httpServletRequest, COOKIE_NAME);

        /* test */
        assertThat(result).isNotNull();
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(cookie);
    }

    @Test
    void get_cookie_with_http_request_cookies_empty_returns_optional_empty() {
        /* execute */
        Optional<Cookie> result = CookieHelper.getCookie(new MockHttpServletRequest(), COOKIE_NAME);

        /* test */
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void get_cookie_with_http_request_cookies_not_containing_desired_cookie_returns_optional_empty() {
        /* prepare */
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        Cookie cookie = CookieHelper.createCookie("otherCookie", COOKIE_VALUE, DURATION, BASE_PATH);
        httpServletRequest.setCookies(cookie);

        /* execute */
        Optional<Cookie> result = CookieHelper.getCookie(httpServletRequest, COOKIE_NAME);

        /* test */
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void remove_cookie() {
        /* prepare */
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();

        String cookie1Name = "cookie1";
        Cookie cookie1 = CookieHelper.createCookie(cookie1Name, COOKIE_VALUE, DURATION, BASE_PATH);
        httpServletResponse.addCookie(cookie1);

        String cookie2Name = "cookie2";
        Cookie cookie2 = CookieHelper.createCookie(cookie2Name, COOKIE_VALUE, DURATION, BASE_PATH);
        httpServletResponse.addCookie(cookie2);

        /* execute */
        CookieHelper.removeCookie(httpServletResponse, cookie1Name);

        /* test */
        Cookie[] result = httpServletResponse.getCookies();

        /*
         * The original cookies should still be present
         */
        assertThat(result).hasSize(3);
        assertThat(result).contains(cookie1, cookie2);

        /*
         * The cookie to be removed should be present as well, but with an empty value and a max age of 0
         */
        Cookie cookieToBeRemoved = result[2];
        assertThat(cookieToBeRemoved).isNotEqualTo(cookie1);
        assertThat(cookieToBeRemoved.getName()).isEqualTo(cookie1Name);
        assertThat(cookieToBeRemoved.getValue()).isEmpty();
        assertThat(cookieToBeRemoved.getMaxAge()).isZero();
        assertThat(cookieToBeRemoved.getPath()).isEqualTo(BASE_PATH);
    }
}
