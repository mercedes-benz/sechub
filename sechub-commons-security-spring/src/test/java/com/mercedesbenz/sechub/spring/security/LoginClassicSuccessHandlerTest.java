// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;

class LoginClassicSuccessHandlerTest {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String USERNAME_PASSWORD_ENCRYPTED = "encrypted";
    private static final String USERNAME_PASSWORD_ENCODED = Base64.getEncoder().encodeToString(USERNAME_PASSWORD_ENCRYPTED.getBytes(StandardCharsets.UTF_8));
    private static final AES256Encryption aes256Encryption = mock();
    private static final Duration COOKIE_AGE = Duration.ofHours(24);
    private static final LoginRedirectHandler loginRedirectHandler = mock();
    private static final MockHttpServletRequest request = new MockHttpServletRequest();
    private static final MockHttpServletResponse response = mock();
    private static final Authentication authentication = mock();
    private static final LoginClassicSuccessHandler handlerToTest = new LoginClassicSuccessHandler(aes256Encryption, COOKIE_AGE, loginRedirectHandler);

    @BeforeEach
    void beforeEach() {
        request.addParameter(USERNAME, USERNAME);
        request.addParameter(PASSWORD, PASSWORD);
        reset(response, authentication, loginRedirectHandler);
        when(aes256Encryption.encrypt("%s:%s".formatted(USERNAME, PASSWORD))).thenReturn(USERNAME_PASSWORD_ENCRYPTED.getBytes());
    }

    @Test
    void on_authentication_success_creates_cookie_and_redirects() throws IOException {
        /* execute */
        handlerToTest.onAuthenticationSuccess(request, response, authentication);

        /* test */

        InOrder inOrder = inOrder(aes256Encryption, response, loginRedirectHandler);
        inOrder.verify(aes256Encryption).encrypt("%s:%s".formatted(USERNAME, PASSWORD));
        inOrder.verify(response).addCookie(assertArg(cookie -> {
            assertThat(cookie.getName()).isEqualTo(AbstractSecurityConfiguration.CLASSIC_AUTH_COOKIE_NAME);
            assertThat(cookie.getValue()).isEqualTo(USERNAME_PASSWORD_ENCODED);
            assertThat(cookie.getMaxAge()).isEqualTo(COOKIE_AGE.getSeconds());
            assertThat(cookie.getPath()).isEqualTo("/");
        }));
        inOrder.verify(loginRedirectHandler).redirect(request, response);
    }

}