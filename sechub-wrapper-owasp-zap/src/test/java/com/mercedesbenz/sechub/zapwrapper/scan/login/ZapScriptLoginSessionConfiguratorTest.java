// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan.login;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openqa.selenium.Cookie;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.zapwrapper.internal.scan.ClientApiWrapper;

class ZapScriptLoginSessionConfiguratorTest {

    private ZapScriptLoginSessionConfigurator sessionConfiguratorToTest;

    private ClientApiWrapper clientApiWrapper;
    private JWTSupport jwtSupport;

    private ApiResponse zapApiResponse;

    private static final String TARGET_URL = "http://example.com";
    private static final boolean FOLLOW_REDIRECTS = true;

    @BeforeEach
    void beforeEach() {
        zapApiResponse = mock();
        jwtSupport = mock();

        clientApiWrapper = mock(ClientApiWrapper.class, new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                // Return the same response for any method call
                return zapApiResponse;
            }
        });

        sessionConfiguratorToTest = new ZapScriptLoginSessionConfigurator(jwtSupport);
    }

    @Test
    void one_cookie_and_one_jwt_results_in_each_mock_called_once() throws ClientApiException {
        /* prepare */
        Cookie cookie = new Cookie("name", "value");
        Set<Cookie> cookies = Set.of(cookie);

        Map<String, String> storage = new HashMap<>();
        storage.put("jwt", "1234");

        ScriptLoginResult loginResult = new ScriptLoginResult();
        loginResult.setSessionCookies(cookies);
        loginResult.setSessionStorage(storage);

        when(jwtSupport.isJWT(storage.get("jwt"))).thenReturn(true);

        /* execute */
        sessionConfiguratorToTest.passSessionDataToZAP(loginResult, TARGET_URL, clientApiWrapper);

        /* test */
        verify(clientApiWrapper).removeHTTPSession(eq(TARGET_URL), any());
        verify(clientApiWrapper).removeHTTPSessionToken(eq(TARGET_URL), any());
        verify(clientApiWrapper).removeReplacerRule(any());
        verify(clientApiWrapper).addHTTPSessionToken(eq(TARGET_URL), any());
        verify(clientApiWrapper).createEmptyHTTPSession(eq(TARGET_URL), any());
        verify(clientApiWrapper).setHTTPSessionTokenValue(eq(TARGET_URL), any(), eq(cookie.getName()), eq(cookie.getValue()));
        verify(clientApiWrapper).setActiveHTTPSession(eq(TARGET_URL), any());
        verify(clientApiWrapper).addReplacerRule(any(), anyBoolean(), any(), anyBoolean(), any(), any(), any(), any());

        verify(clientApiWrapper).accessUrlViaZap(TARGET_URL, FOLLOW_REDIRECTS);
    }

    @Test
    void no_cookie_and_no_jwt_results_clienapiwrapper_not_adding_replacer_rule() throws ClientApiException {
        /* prepare */
        Map<String, String> storage = new HashMap<>();
        storage.put("jwt", "1234");
        ScriptLoginResult loginResult = new ScriptLoginResult();
        loginResult.setSessionStorage(storage);

        when(jwtSupport.isJWT(anyString())).thenReturn(false);

        /* execute */
        sessionConfiguratorToTest.passSessionDataToZAP(loginResult, TARGET_URL, clientApiWrapper);

        /* test */
        // no JWT can be added
        verify(clientApiWrapper, never()).addReplacerRule(any(), anyBoolean(), any(), anyBoolean(), any(), any(), any(), any());
        // no cookie can be added
        verify(clientApiWrapper, never()).setHTTPSessionTokenValue(eq(TARGET_URL), any(), any(), any());

        verify(clientApiWrapper).removeHTTPSession(eq(TARGET_URL), any());
        verify(clientApiWrapper).removeHTTPSessionToken(eq(TARGET_URL), any());
        verify(clientApiWrapper).removeReplacerRule(any());
        verify(clientApiWrapper).addHTTPSessionToken(eq(TARGET_URL), any());
        verify(clientApiWrapper).createEmptyHTTPSession(eq(TARGET_URL), any());
        verify(clientApiWrapper).setActiveHTTPSession(eq(TARGET_URL), any());

        verify(clientApiWrapper).accessUrlViaZap(TARGET_URL, FOLLOW_REDIRECTS);
    }

    @Test
    void zap_available_clean_up_old_session_data_does_not_throw_an_exception() throws ClientApiException {
        /* execute + test */
        assertDoesNotThrow(() -> sessionConfiguratorToTest.cleanUpOldSessionDataIfNecessary(TARGET_URL, clientApiWrapper));
    }

    @Test
    void zap_not_available_removing_old_session_does_throw_an_exception() throws ClientApiException {
        String errorMessage = "Connection refused";

        ClientApiException apiException = new ClientApiException(errorMessage);

        when(clientApiWrapper.removeHTTPSession(any(), any())).thenThrow(apiException);

        /* execute + test */
        assertThrows(ClientApiException.class, () -> sessionConfiguratorToTest.cleanUpOldSessionDataIfNecessary(TARGET_URL, clientApiWrapper));
    }

    @Test
    void zap_not_available_removing_old_session_token_does_throw_an_exception() throws ClientApiException {
        String errorMessage = "Connection refused";

        ClientApiException apiException = new ClientApiException(errorMessage);

        when(clientApiWrapper.removeHTTPSessionToken(any(), any())).thenThrow(apiException);

        /* execute + test */
        assertThrows(ClientApiException.class, () -> sessionConfiguratorToTest.cleanUpOldSessionDataIfNecessary(TARGET_URL, clientApiWrapper));
    }

    @Test
    void zap_not_available_removing_old_jwt_replacer_rule_does_throw_an_exception() throws ClientApiException {
        String errorMessage = "Connection refused";

        ClientApiException apiException = new ClientApiException(errorMessage);

        when(clientApiWrapper.removeReplacerRule(any())).thenThrow(apiException);

        /* execute + test */
        assertThrows(ClientApiException.class, () -> sessionConfiguratorToTest.cleanUpOldSessionDataIfNecessary(TARGET_URL, clientApiWrapper));
    }
}
