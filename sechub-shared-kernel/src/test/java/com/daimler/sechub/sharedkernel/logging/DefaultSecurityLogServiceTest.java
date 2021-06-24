// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.logging;

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
import org.springframework.web.context.request.ServletRequestAttributes;

import com.daimler.sechub.sharedkernel.UserContextService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class DefaultSecurityLogServiceTest {

    private static final String VALUE2_S = "value2-s";
    private static final String VALUE1_S = "value1-s";
    private static final String KEY2_S = "key2-s";
    private static final String KEY1_S = "key1-s";
    private static final String VALUE2 = "value2";
    private static final String VALUE1 = "value1";
    private static final String KEY2 = "key2";
    private static final String KEY1 = "key1";
    private DefaultSecurityLogService serviceToTest;
    private Logger logger;
    private LogSanitizer logsanitzer;
    private UserContextService userContextService;
    private RequestAttributesProvider requestAttributesProvider;
    private HttpSession httpSession;
    private HttpServletRequest request;

    @BeforeEach
    void beforeEach() {
        logger = mock(Logger.class);

        // fake logsanitizer
        logsanitzer = mock(LogSanitizer.class);
        when(logsanitzer.sanitize(eq(KEY1),any(Integer.class))).thenReturn(KEY1_S);
        when(logsanitzer.sanitize(eq(KEY2),any(Integer.class))).thenReturn(KEY2_S);
        
        when(logsanitzer.sanitize(eq(VALUE1),any(Integer.class),eq(false))).thenReturn(VALUE1_S);
        when(logsanitzer.sanitize(eq(VALUE2),any(Integer.class),eq(false))).thenReturn(VALUE2_S);
        
        // fake user context
        userContextService = mock(UserContextService.class);
        
        // fake http data providers
        requestAttributesProvider = mock(RequestAttributesProvider.class);
        request=mock(HttpServletRequest.class);
        ServletRequestAttributes attributes = new ServletRequestAttributes(request); // final methods, so not by mockito
        httpSession = mock(HttpSession.class);
        
        when(requestAttributesProvider.getRequestAttributes()).thenReturn(attributes);
        when(request.getSession()).thenReturn(httpSession);
        
        Map<String, String> map = new LinkedHashMap<>();
        map.put(KEY1,VALUE1);
        map.put(KEY2,VALUE2);
        
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

        for (String key: map.keySet()) {
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
        serviceToTest.requestAttributesProvider=requestAttributesProvider;

    }

    @Test
    void log_with_null_values_possible_logger_warns_about_missing_log_type() {
        /* execute */
        serviceToTest.log((SecurityLogType) null, (String) null, (Object[]) null);

        /* test */
        verify(logger).warn("Security log service was called with no log type, so call was wrong implemented! Use fallback:{}", SecurityLogType.UNKNOWN);
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
        
        // http headers from session
        JsonNode httpHeaders = jsonNode.get("httpHeaders");
        JsonNode key1 = httpHeaders.get(KEY1_S);
        assertEquals(VALUE1_S, key1.textValue());
        
        JsonNode key2 = httpHeaders.get(KEY2_S);
        assertEquals(VALUE2_S, key2.textValue());
        

        // test third parameter
        assertEquals("param1", messageParamCaptor.getValue());
    }

}
