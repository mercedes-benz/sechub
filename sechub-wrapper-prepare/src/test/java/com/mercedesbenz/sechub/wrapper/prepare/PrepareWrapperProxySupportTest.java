// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PrepareWrapperProxySupportTest {

    PrepareWrapperProxySupport supportToTest;

    PrepareWrapperSystemPropertySupport propertySupport;

    @BeforeEach
    void beforeEach() {
        supportToTest = new PrepareWrapperProxySupport();
        propertySupport = mock(PrepareWrapperSystemPropertySupport.class);

        supportToTest.propertySupport = propertySupport;
        supportToTest.proxyEnabled = true;
    }

    @Test
    void systemProperties_are_set_correct() {
        /* prepare */
        supportToTest.noProxy = "some.example,example.org";
        supportToTest.httpsProxy = "some.example.proxy:8080";

        /* execute */
        supportToTest.setUpProxy();

        /* test */
        verify(propertySupport).setSystemProperty("https.proxyHost", "some.example.proxy");
        verify(propertySupport).setSystemProperty("https.proxyPort", "8080");
        verify(propertySupport).setSystemProperty("https.nonProxyHosts", "some.example|example.org");
    }

    @Test
    void systemProperties_are_set_when_noProxyList_is_empty() {
        /* prepare */
        supportToTest.noProxy = "";
        supportToTest.httpsProxy = "some.example.proxy:8080";

        /* execute */
        supportToTest.setUpProxy();

        /* test */
        verify(propertySupport).setSystemProperty("https.proxyHost", "some.example.proxy");
        verify(propertySupport).setSystemProperty("https.proxyPort", "8080");
        verify(propertySupport).setSystemProperty("https.nonProxyHosts", "");
    }

    @Test
    void setProxySystemProperty_throws_exception_when_port_not_valid() {
        /* prepare */
        supportToTest.httpsProxy = "some.example.proxy:invalidPort";

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> supportToTest.setUpProxy());

        /* test */
        assertTrue(exception.getMessage().contains("Port number invalidPort is not a number. Please set the environment variable: "));
    }

    @Test
    void setProxySystemProperty_throws_exception_when_port_is_empty() {
        /* prepare */
        supportToTest.httpsProxy = "some.example.proxy:";

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> supportToTest.setUpProxy());

        /* test */
        assertTrue(exception.getMessage().contains("No port number is set. Please set the environment variable: "));
    }

    @Test
    void setProxySystemProperty_throws_exception_when_proxy_not_set() {
        /* prepare */
        supportToTest.httpsProxy = "";

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> supportToTest.setUpProxy());

        /* test */
        assertTrue(exception.getMessage().contains("No HTTPS proxy is set. Please set the environment variable: "));
    }
}