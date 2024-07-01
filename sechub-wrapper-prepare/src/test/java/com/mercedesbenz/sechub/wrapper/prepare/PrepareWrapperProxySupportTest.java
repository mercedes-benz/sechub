package com.mercedesbenz.sechub.wrapper.prepare;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest
    @ValueSource(strings = { "some.example.url/my-example-page", "https://some.example/repository.git", "https://example.org/path" })
    void systemProperties_are_not_set_when_url_is_in_noProxyList(String url) {
        /* prepare */
        supportToTest.noProxy = "some.example,example.org";
        supportToTest.httpsProxy = "some.example.proxy:8080";

        /* execute */
        supportToTest.setUpProxy(url);

        /* test */
        verify(propertySupport, never()).setSystemProperty("https.proxyHost", "some.example.proxy");
        verify(propertySupport, never()).setSystemProperty("https.proxyPort", "8080");
    }

    @ParameterizedTest
    @ValueSource(strings = { "some.example.url/my-example-page", "https://some.example/repository.git", "https://example.org/path" })
    void systemProperties_are_set_when_when_url_is_not_in_noProxyList(String url) {
        /* prepare */
        supportToTest.noProxy = "notMyUrl,some.other.example.com";
        supportToTest.httpsProxy = "some.example.proxy:8080";

        /* execute */
        supportToTest.setUpProxy(url);

        /* test */
        verify(propertySupport).setSystemProperty("https.proxyHost", "some.example.proxy");
        verify(propertySupport).setSystemProperty("https.proxyPort", "8080");
    }

    @ParameterizedTest
    @ValueSource(strings = { "some.example.url/my-example-page", "https://some.example/repository.git", "https://example.org/path" })
    void systemProperties_are_set_when_noProxyList_is_empty(String url) {
        /* prepare */
        supportToTest.noProxy = "";
        supportToTest.httpsProxy = "some.example.proxy:8080";

        /* execute */
        supportToTest.setUpProxy(url);

        /* test */
        verify(propertySupport).setSystemProperty("https.proxyHost", "some.example.proxy");
        verify(propertySupport).setSystemProperty("https.proxyPort", "8080");
    }

    @Test
    void setProxySystemProperty_sets_https_proxyHost_and_proxyPort() {
        /* prepare */
        supportToTest.httpsProxy = "some.example.proxy:8080";

        /* execute */
        supportToTest.setUpProxy("https://some.example/repository.git");

        /* test */
        verify(propertySupport).setSystemProperty("https.proxyHost", "some.example.proxy");
        verify(propertySupport).setSystemProperty("https.proxyPort", "8080");
    }

    @Test
    void setProxySystemProperty_throws_exception_when_port_not_valid() {
        /* prepare */
        supportToTest.httpsProxy = "some.example.proxy:invalidPort";

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> supportToTest.setUpProxy("https://some.example/repository.git"));

        /* test */
        assertTrue(exception.getMessage().contains("Port number is not a number. Please set the environment variable: "));
    }

    @Test
    void setProxySystemProperty_throws_exception_when_proxy_not_set() {
        /* prepare */
        supportToTest.httpsProxy = "";

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> supportToTest.setUpProxy("https://some.example/repository.git"));

        /* test */
        assertTrue(exception.getMessage().contains("No HTTPS proxy is set. Please set the environment variable: "));
    }
}