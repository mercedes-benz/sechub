package com.mercedesbenz.sechub.spring.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginClassicSuccessHandlerTest {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String USERNAME_PASSWORD_ENCRYPTED = "encrypted";
    private static final String USERNAME_PASSWORD_ENCODED = Base64.getEncoder().encodeToString(USERNAME_PASSWORD_ENCRYPTED.getBytes(StandardCharsets.UTF_8));
    private static final String REDIRECT_URI = "redirect-uri";
    private static final UserDetails userDetails = new TestUserDetails(List.of(), USERNAME, "{noop}" + PASSWORD);
    private static final AES256Encryption aes256Encryption = mock();
    private static final MockHttpServletRequest request = new MockHttpServletRequest();
    private static final MockHttpServletResponse response = mock();
    private static final Authentication authentication = mock();
    private static final LoginClassicSuccessHandler handlerToTest = new LoginClassicSuccessHandler(REDIRECT_URI, aes256Encryption);

    @BeforeEach
    void beforeEach() {
        reset(response, authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(aes256Encryption.encrypt("%s:%s".formatted(USERNAME, PASSWORD))).thenReturn(USERNAME_PASSWORD_ENCRYPTED.getBytes());
    }

    @Test
    void on_authentication_success_creates_cookie_and_redirects() throws IOException {
        /* execute */
        handlerToTest.onAuthenticationSuccess(request, response, authentication);

        /* test */
        verify(aes256Encryption).encrypt("%s:%s".formatted(USERNAME, PASSWORD));

        InOrder inOrder = inOrder(response);
        inOrder.verify(response).addCookie(assertArg(cookie -> {
            assertThat(cookie.getName()).isEqualTo(AbstractSecurityConfiguration.CLASSIC_AUTH_COOKIE_NAME);
            assertThat(cookie.getValue()).isEqualTo(USERNAME_PASSWORD_ENCODED);
            assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofHours(1).toSeconds());
            assertThat(cookie.getPath()).isEqualTo("/");
        }));
        inOrder.verify(response).sendRedirect(REDIRECT_URI);
    }

}