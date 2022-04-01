// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.helper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;

import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitRuntimeException;

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
        assertThrows(MustExitRuntimeException.class, () -> factoryToTest.create(url));
    }

    @Test
    void ending_slash() {
        /* prepare */
        String url = "http://127.0.0.1/";

        /* execute */
        URI targetUri = factoryToTest.create(url);

        /* test */
        assertTrue("http://127.0.0.1".equals(targetUri.toString()));

    }

    @ParameterizedTest
    @CsvSource({ "https://127.0.0.1:7777/#,https://127.0.0.1:7777", "https://127.0.0.1:7777/#/,https://127.0.0.1:7777",
            "https://127.0.0.1/#,https://127.0.0.1", })
    void ending_slash_and_hash(String url, String expectedTargetURI) {

        /* execute */
        URI targetUri = factoryToTest.create(url);

        /* test */
        assertTrue(expectedTargetURI.equals(targetUri.toString()));
    }

    @Test
    void normalize_url_correctly() {
        /* prepare */
        String url = "https://127.0.0.1:7777/profile/search?name=";

        /* execute */
        URI targetUri = factoryToTest.create(url);

        /* test */
        assertTrue("https://127.0.0.1:7777/profile/search".equals(targetUri.toString()));
    }

    @Test
    void removes_hash_from_url_correctly() {
        /* prepare */
        String url = "https://127.0.0.1:7777/#/user/profile";

        /* execute */
        URI targetUri = factoryToTest.create(url);

        /* test */
        assertTrue("https://127.0.0.1:7777/user/profile".equals(targetUri.toString()));
    }

    @Test
    void removes_hashbang_correctly() {
        /* prepare */
        String url = "https://my.personal.uri/profile/#!771252/peter";

        /* execute */
        URI targetUri = factoryToTest.create(url);

        /* test */
        assertTrue("https://my.personal.uri/profile".equals(targetUri.toString()));
    }
}
