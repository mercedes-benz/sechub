// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class LoginRedirectHandlerTest {

    private static final String DEFAULT_REDIRECT_URI = "http://default-redirect.org";
    private static final HttpServletRequest httpServletRequest = mock();
    private static final HttpServletResponse httpServletResponse = mock();
    private static final LoginRedirectHandler handlerToTest = new LoginRedirectHandler(DEFAULT_REDIRECT_URI);
    private static final String REDIRECT_PATH_PARAMETER = "redirectPath";

    @BeforeEach
    void beforeEach() {
        reset(httpServletRequest, httpServletResponse);
    }

    @Test
    void redirect_with_no_redirect_path_parameter_should_redirect_to_default_uri() throws IOException {
        /* prepare */
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("someRandomParam", new String[] { "value1", "value2" });
        parameterMap.put("anotherRandomParam", new String[] { "value3" });
        when(httpServletRequest.getParameterMap()).thenReturn(parameterMap);

        /* execute */
        handlerToTest.redirect(httpServletRequest, httpServletResponse);

        /* test */
        verify(httpServletResponse).sendRedirect(DEFAULT_REDIRECT_URI);
    }

    @Test
    void redirect_with_valid_redirect_path_parameter_should_redirect_to_specified_path() throws IOException {
        /* prepare */
        String redirectPath = "/local/path";
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put(REDIRECT_PATH_PARAMETER, new String[] { redirectPath });
        parameterMap.put("someRandomParam", new String[] { "value1", "value2" });
        parameterMap.put("anotherRandomParam", new String[] { "value3" });
        when(httpServletRequest.getParameterMap()).thenReturn(parameterMap);

        /* execute */
        handlerToTest.redirect(httpServletRequest, httpServletResponse);

        /* test */
        verify(httpServletResponse).sendRedirect(redirectPath + "?someRandomParam=value1&someRandomParam=value2&anotherRandomParam=value3");
    }

    @ParameterizedTest
    @ValueSource(strings = { "http://external/path", "https://another-external/path", "https://example.com/resource", "example.com" })
    void redirect_with_invalid_redirect_path_parameter_should_throw_response_status_exception(String redirectPath) {
        /* prepare */
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put(REDIRECT_PATH_PARAMETER, new String[] { redirectPath });
        when(httpServletRequest.getParameterMap()).thenReturn(parameterMap);

        /* execute + test */
        assertThatThrownBy(() -> handlerToTest.redirect(httpServletRequest, httpServletResponse)).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid redirect path: " + redirectPath);
    }

}