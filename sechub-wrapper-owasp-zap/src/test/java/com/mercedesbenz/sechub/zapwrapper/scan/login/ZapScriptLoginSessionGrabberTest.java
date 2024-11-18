// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan.login;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.zapwrapper.internal.scan.ClientApiSupport;

class ZapScriptLoginSessionGrabberTest {

    private ZapScriptLoginSessionGrabber sessionGrabberToTest;

    private ClientApiSupport clientApiSupport;
    private FirefoxDriver firefox;
    private Options webDriverOptions;

    private ApiResponse zapApiResponse;

    private static final String TARGET_URL = "http://example.com";
    private static final String FOLLOW_REDIRECTS = "true";

    @BeforeEach
    void beforeEach() {
        zapApiResponse = mock();
        firefox = mock();
        webDriverOptions = mock();

        clientApiSupport = mock(ClientApiSupport.class, new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                // Return the same response for any method call
                return zapApiResponse;
            }
        });

        sessionGrabberToTest = new ZapScriptLoginSessionGrabber();
    }

    @Test
    void one_cookie_and_one_jwt_results_in_each_mock_called_once() throws ClientApiException {
        /* prepare */
        Cookie cookie = new Cookie("name", "value");
        Set<Cookie> cookies = Set.of(cookie);

        Map<String, String> storage = new HashMap<>();
        // example from https://jwt.io/
        storage.put("jwt",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");

        when(firefox.manage()).thenReturn(webDriverOptions);

        when(webDriverOptions.getCookies()).thenReturn(cookies);
        when(firefox.executeScript(anyString())).thenReturn(storage);

        /* execute */
        sessionGrabberToTest.extractSessionAndPassToZAP(firefox, TARGET_URL, clientApiSupport);

        /* test */
        verify(firefox, times(1)).manage();
        verify(webDriverOptions, times(1)).getCookies();
        verify(firefox, times(1)).executeScript(anyString());

        verify(clientApiSupport, times(1)).removeHTTPSession(eq(TARGET_URL), any());
        verify(clientApiSupport, times(1)).removeHTTPSessionToken(eq(TARGET_URL), any());
        verify(clientApiSupport, times(1)).removeReplacerRule(any());
        verify(clientApiSupport, times(1)).addHTTPSessionToken(eq(TARGET_URL), any());
        verify(clientApiSupport, times(1)).createEmptyHTTPSession(eq(TARGET_URL), any());
        verify(clientApiSupport, times(1)).setHTTPSessionTokenValue(eq(TARGET_URL), any(), eq(cookie.getName()), eq(cookie.getValue()));
        verify(clientApiSupport, times(1)).setActiveHTTPSession(eq(TARGET_URL), any());
        verify(clientApiSupport, times(1)).addReplacerRule(any(), any(), any(), any(), any(), any(), any(), any());

        verify(clientApiSupport, times(1)).accessUrlViaZap(TARGET_URL, FOLLOW_REDIRECTS);
    }

    @Test
    void no_cookie_and_no_jwt_results_clienapisupport_not_adding_replacer_rule() throws ClientApiException {
        /* prepare */
        when(firefox.manage()).thenReturn(webDriverOptions);

        when(webDriverOptions.getCookies()).thenReturn(Collections.emptySet());
        when(firefox.executeScript(anyString())).thenReturn(Collections.emptyMap());

        /* execute */
        sessionGrabberToTest.extractSessionAndPassToZAP(firefox, TARGET_URL, clientApiSupport);

        /* test */
        // both browser storages are checked now without JWT
        verify(firefox, times(2)).executeScript(anyString());
        // no JWT can be added
        verify(clientApiSupport, never()).addReplacerRule(any(), any(), any(), any(), any(), any(), any(), any());
        // no cookie can be added
        verify(clientApiSupport, never()).setHTTPSessionTokenValue(eq(TARGET_URL), any(), any(), any());

        // the other calls must be the same as on every execution without error
        verify(firefox, times(1)).manage();
        verify(webDriverOptions, times(1)).getCookies();

        verify(clientApiSupport, times(1)).removeHTTPSession(eq(TARGET_URL), any());
        verify(clientApiSupport, times(1)).removeHTTPSessionToken(eq(TARGET_URL), any());
        verify(clientApiSupport, times(1)).removeReplacerRule(any());
        verify(clientApiSupport, times(1)).addHTTPSessionToken(eq(TARGET_URL), any());
        verify(clientApiSupport, times(1)).createEmptyHTTPSession(eq(TARGET_URL), any());
        verify(clientApiSupport, times(1)).setActiveHTTPSession(eq(TARGET_URL), any());

        verify(clientApiSupport, times(1)).accessUrlViaZap(TARGET_URL, FOLLOW_REDIRECTS);
    }

}
