// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.helper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;

import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;

class BaseTargetUriFactoryTest {

    private BaseTargetUriFactory factoryToTest;

    @BeforeEach
    void beforeEach() {
        factoryToTest = new BaseTargetUriFactory();
    }

    @ParameterizedTest
    @CsvSource({ "127.0.0.1", "127.0.0.1:8080", "htts://172.118.11.23", "htts://172.118.11.23:8080", "https:172.118.11.23", "http:172.118.11.23:8080" })
    @NullSource
    void throws_mustexitruntimeexception_for_invalid_urls(String url) {

        /* test */
        assertThrows(ZapWrapperRuntimeException.class, () -> factoryToTest.create(url));
    }

    @Test
    void ending_slash() {
        /* prepare */
        String url = "http://127.0.0.1/";

        /* execute */
        URL targetUrl = factoryToTest.create(url);

        /* test */
        assertTrue("http://127.0.0.1".equals(targetUrl.toString()));

    }

    @ParameterizedTest
    @CsvSource({ "https://127.0.0.1:7777/#,https://127.0.0.1:7777", "https://127.0.0.1:7777/#/,https://127.0.0.1:7777",
            "https://127.0.0.1/#,https://127.0.0.1", })
    void ending_slash_and_hash(String url, String expectedTargetURI) {

        /* execute */
        URL targetUrl = factoryToTest.create(url);

        /* test */
        assertTrue(expectedTargetURI.equals(targetUrl.toString()));
    }

    @Test
    void normalize_url_correctly() {
        /* prepare */
        String url = "https://127.0.0.1:7777/profile/search?name=";

        /* execute */
        URL targetUrl = factoryToTest.create(url);

        /* test */
        assertTrue("https://127.0.0.1:7777/profile/search".equals(targetUrl.toString()));
    }

    @Test
    void removes_hash_from_url_correctly() {
        /* prepare */
        String url = "https://127.0.0.1:7777/#/user/profile";

        /* execute */
        URL targetUrl = factoryToTest.create(url);

        /* test */
        assertTrue("https://127.0.0.1:7777/user/profile".equals(targetUrl.toString()));
    }

    @Test
    void removes_hashbang_correctly() {
        /* prepare */
        String url = "https://my.personal.uri/profile/#!771252/peter";

        /* execute */
        URL targetUrl = factoryToTest.create(url);

        /* test */
        assertTrue("https://my.personal.uri/profile".equals(targetUrl.toString()));
    }
}
