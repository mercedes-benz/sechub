// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.pds.commons.core.PDSLogSanitizer;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperUsageException;

class GitLocationConverterTest {

    private GitLocationConverter converterToTest;
    private PDSLogSanitizer pdsLogSanitizer;

    @BeforeEach
    void beforeEach() {
        converterToTest = new GitLocationConverter();

        pdsLogSanitizer = mock(PDSLogSanitizer.class);
        converterToTest.pdsLogSanitizer = pdsLogSanitizer;
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://example.com/mygit.git", "https://example.com:8443/subdir/my-other.git", "https://github.com/example-org/example1.git" })
    void https_url_is_returned_as_is(String httpsUrl) {
        /* execute */
        URL result = converterToTest.convertLocationToHttpsBasedURL(httpsUrl);

        /* test */
        assertEquals(httpsUrl, result.toExternalForm());
    }

    @Test
    void git_url_with_at_is_returned_as_https() {
        /* execute */
        URL result = converterToTest.convertLocationToHttpsBasedURL("git@github.com:example/example1.git");

        /* test */
        assertEquals("https://github.com/example/example1.git", result.toExternalForm());
    }

    @Test
    void git_url_as_protocol_is_returned_as_https() {
        /* execute */
        URL result = converterToTest.convertLocationToHttpsBasedURL("git://github.com:example/example1.git");

        /* test */
        assertEquals("https://github.com/example/example1.git", result.toExternalForm());
    }

    @Test
    void ssh_url_is_returned_as_https() {
        /* execute */
        URL result = converterToTest.convertLocationToHttpsBasedURL("ssh://login@server.example.com:12345/absolute/path/to/repository");

        /* test */
        assertEquals("https://server.example.com:443/absolute/path/to/repository", result.toExternalForm());
    }

    @Test
    void ftp_url_is_throwing_an_prepare_wrapper_usage_exception() {
        /* execute + test */
        PrepareWrapperUsageException exception = assertThrows(PrepareWrapperUsageException.class,
                () -> converterToTest.convertLocationToHttpsBasedURL("ftp://public.ftp-servers.example.com/mydirectory/myfile.txt"));

        assertTrue(exception.getMessage().contains("Location could not be transferred"));
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    void empty_or_null_url_is_throwing_an_illegal_argument_exception(String location) {
        /* execute + test */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> converterToTest.convertLocationToHttpsBasedURL(location));

        assertTrue(exception.getMessage().contains("Location may not"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://github.com:example/example1.git", "http://github.com:example/example1.git", "git@github.com:example/example1.git",
            "http://github.com:example/example1.git" })
    void convertLocationForRepositoryName(String location) {
        /* execute */
        String result = converterToTest.convertLocationForRepositoryName(location);

        /* test */
        assertEquals("example1", result);
    }
}
