// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.logging;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.sharedkernel.UserContextService;

class DefaultSecurityLogServiceTest {

    private static final String OBFUSCATED = "obfuscated";
    private static final String SANITIZED = "sanitized";
    private static final String KEY_AUTHORIZE = HttpHeaders.AUTHORIZATION;
    private static final String KEY_AUTHORIZE_mixcased = "auThorization";
    private static final String VALUE2 = "value2";
    private static final String VALUE1 = "value1";
    private static final String VALUE_AUTHORIZE = "Basic aW350LXalc3RexampleXWzZXI6aW50LXRlc3Rfb23seXVzZXtHdk";

    private static final String KEY2 = "key2";
    private static final String KEY1 = "key1";
    private DefaultSecurityLogService serviceToTest;
    private Logger logger;
    private LogSanitizer logsanitzer;
    private UserContextService userContextService;
    private RequestAttributesProvider requestAttributesProvider;
    private HttpSession httpSession;
    private HttpServletRequest request;
    private AuthorizeValueObfuscator authorizedValueObfuscator;
    private BasicAuthUserExtraction basicAuthUserExtraction;

    @BeforeEach
    void beforeEach() {
        logger = mock(Logger.class);

        userContextService = mock(UserContextService.class);
        basicAuthUserExtraction = mock(BasicAuthUserExtraction.class);
        when(basicAuthUserExtraction.extractUserFromAuthHeader(VALUE_AUTHORIZE)).thenReturn("resolved-basic-auth-user");

        // mock logsanitizer
        logsanitzer = mock(LogSanitizer.class);
        when(logsanitzer.sanitize(anyString(), anyInt())).thenAnswer(i -> SANITIZED + i.getArguments()[0]);
        when(logsanitzer.sanitize(anyString(), anyInt(), anyBoolean())).thenAnswer(i -> SANITIZED + i.getArguments()[0]);

        // mock obfuscation
        authorizedValueObfuscator = mock(AuthorizeValueObfuscator.class);
        when(authorizedValueObfuscator.obfuscate(anyString(), anyInt())).thenAnswer(i -> "obfuscated" + i.getArguments()[0]);

        // mock http data providers
        requestAttributesProvider = mock(RequestAttributesProvider.class);
        request = mock(HttpServletRequest.class);
        ServletRequestAttributes attributes = new ServletRequestAttributes(request); // final methods, so not by mockito
        httpSession = mock(HttpSession.class);
        when(request.getHeader("authorization")).thenReturn(VALUE_AUTHORIZE);
        when(request.getRemoteAddr()).thenReturn("fake-remote-addr");
        when(httpSession.getId()).thenReturn("fake-http-session-id");
        when(request.getRequestURI()).thenReturn("fake-request-uri");

        when(requestAttributesProvider.getRequestAttributes()).thenReturn(attributes);
        when(request.getSession()).thenReturn(httpSession);

        Map<String, String> map = new LinkedHashMap<>();
        map.put(KEY1, VALUE1);
        map.put(KEY2, VALUE2);
        map.put(KEY_AUTHORIZE, VALUE_AUTHORIZE);
        map.put(KEY_AUTHORIZE_mixcased, VALUE_AUTHORIZE);

        Iterator<String> it = map.keySet().iterator();
        when(request.getHeaderNames()).thenReturn(new Enumeration<String>() {

            @Override
            public String nextElement() {
                return it.next();
            }

            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }
        });

        for (String key : map.keySet()) {
            when(request.getHeader(key)).thenReturn(map.get(key));

        }

        /* service to test uses a mocked logger */
        serviceToTest = new DefaultSecurityLogService() {
            Logger getLogger() {
                return logger;
            };
        };
        serviceToTest.logSanititzer = logsanitzer;
        serviceToTest.userContextService = userContextService;
        serviceToTest.requestAttributesProvider = requestAttributesProvider;
        serviceToTest.authorizedValueObfuscator = authorizedValueObfuscator;
        serviceToTest.basicAuthUserExtraction = basicAuthUserExtraction;
    }

    @Test
    void log_with_null_values_possible_logger_warns_about_missing_log_type() {
        /* execute */
        serviceToTest.log((SecurityLogType) null, (String) null, (Object[]) null);

        /* test */
        verify(logger).warn("Security log service was called with no log type! Using fallback:{}", SecurityLogType.UNKNOWN);
    }

    @Test
    void log_with_expected_setup_writes_ouptut_to_logger_as_expected() throws Exception {
        /* prepare */
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> typeStringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> jsonDataCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageParamCaptor = ArgumentCaptor.forClass(String.class);

        /* execute */
        serviceToTest.log(SecurityLogType.POTENTIAL_INTRUSION, "testmessage={}", "param1");

        /* test */
        verify(logger).warn(messageCaptor.capture(), typeStringCaptor.capture(), jsonDataCaptor.capture(), messageParamCaptor.capture());

        // test message
        String message = messageCaptor.getValue();

        int begin = message.indexOf("[SECURITY] [{}]");
        int data = message.indexOf("data=\n{}");
        int end = message.indexOf(", message=testmessage={}");

        assertNotEquals(-1, begin); // found
        assertNotEquals(-1, data); // found
        assertNotEquals(-1, end); // found

        assertTrue(end > data); // check ordering
        assertTrue(data > begin); // check ordering

        // test first parameter
        String type = typeStringCaptor.getValue();
        assertEquals(SecurityLogType.POTENTIAL_INTRUSION.getTypeId(), type);

        // test second parameter which contains JSON
        String json = jsonDataCaptor.getValue();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json); // at least valid json

        // type
        JsonNode typeNode = jsonNode.get("type");
        assertEquals(SecurityLogType.POTENTIAL_INTRUSION.name(), typeNode.textValue());

        // basicAuthUser
        JsonNode basicAuthUserNode = jsonNode.get("basicAuthUser");
        assertEquals("sanitizedresolved-basic-auth-user", basicAuthUserNode.textValue());

        // http headers from session
        JsonNode httpHeaders = jsonNode.get("httpHeaders");
        JsonNode key1 = httpHeaders.get(SANITIZED + KEY1);
        assertEquals(SANITIZED + VALUE1, key1.textValue());

        JsonNode key2 = httpHeaders.get(SANITIZED + KEY2);
        assertEquals(SANITIZED + VALUE2, key2.textValue());

        // test that authorize is not logged plain inside logs but obfuscated
        JsonNode keyAuth = httpHeaders.get(SANITIZED + KEY_AUTHORIZE);
        assertEquals(SANITIZED + OBFUSCATED + VALUE_AUTHORIZE, keyAuth.textValue());

        JsonNode keyAuth2 = httpHeaders.get(SANITIZED + KEY_AUTHORIZE_mixcased);
        assertEquals(SANITIZED + OBFUSCATED + VALUE_AUTHORIZE, keyAuth2.textValue());

        // test third parameter
        assertEquals("param1", messageParamCaptor.getValue());

        // test some fields which could be tampered as well are also sanitized:
        assertEquals(SANITIZED + "fake-remote-addr", jsonNode.get("clientIp").textValue());
        assertEquals(SANITIZED + "fake-http-session-id", jsonNode.get("sessionId").textValue());
        assertEquals(SANITIZED + "fake-request-uri", jsonNode.get("requestURI").textValue());
    }

}
